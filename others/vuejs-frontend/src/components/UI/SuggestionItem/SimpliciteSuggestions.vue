<template lang="html">
    <div>
        <div v-for="suggestion in suggestions || []" :key="suggestion.row_id">
            <SuggestionItem
                :suggestion="suggestion"
                :inputValue="inputValue"
                @suggestionItemSelected="suggestionSelected"
            />
        </div>
    </div>
</template>

<script>
import SuggestionItem from "./SuggestionItem.vue";
import { mapGetters } from "vuex";

export default {
    name: "SimpliciteSuggestions",
    components: {
        SuggestionItem,
    },
    props: {
        simpliciteHits: Array,
        inputValue: {
            type: String,
            default: "",
        },
    },
    data: function () {
        return {
            suggestions: [],
            contentMaxLength: 1000,
            suggestionMaxLength: 10,
            keyRegex: /(\*|[A-Za-z]+) (.*)/,
            frontPathRegex: /[A-Za-z- _]* (.+)/,
            translate: this.$smp.getBusinessObject("TrnLsnTranslate"),
            lesson: this.$smp.getBusinessObject("TrnLesson"),
            category: this.$smp.getBusinessObject("TrnCategory"),
        };
    },
    computed: {
        ...mapGetters({
            lang: "ui/lang",
        }),
    },
    // async operation is made on component creation
    async created() {
        for (
            let i = 0;
            this.suggestions.length < this.suggestionMaxLength &&
            this.simpliciteHits.length > i;
            i++
        ) {
            await this.setSuggestion(this.simpliciteHits[i].row_id);
        }
    },
    methods: {
        suggestionSelected(suggestion) {
            this.$emit("suggestionSelected", suggestion);
        },
        async setSuggestion(translateRowId) {
            const suggestion = new Object();
            let translateRes = await this.searchTranslate(translateRowId);
            if (translateRes) {
                if (translateRes.trnLtrLang === this.lang) {
                    const lessonRes = await this.searchLesson(
                        translateRes.trnLtrLsnId
                    );
                    if (lessonRes) {
                        await this.validateAndSetSuggestion(
                            suggestion,
                            lessonRes,
                            translateRes
                        );
                    }
                } else if (translateRes.trnLtrLang === "ANY") {
                    const lessonRes = await this.searchLesson(
                        translateRes.trnLtrLsnId
                    );
                    if (lessonRes) {
                        const currentLangTranslate =
                            await this.searchTranslateLsnRowId(
                                lessonRes.row_id,
                                this.lang
                            );
                        if (!currentLangTranslate) {
                            await this.validateAndSetSuggestion(
                                suggestion,
                                lessonRes,
                                translateRes
                            );
                        }
                    }
                }
            }
        },
        async validateAndSetSuggestion(suggestion, lessonRes, translateRes) {
            suggestion.path = lessonRes.trnLsnFrontPath;
            const catPublished = await this.isCategoryPublished(
                lessonRes.trnLsnCatId,
                true
            );
            if (catPublished) {
                if (translateRes.trnLtrRawContent && lessonRes.trnLsnPublish) {
                    this.formatContent(suggestion, translateRes);
                }
            }
        },
        async searchTranslateLang(rowId, lang) {
            const res = await this.translate.search({
                row_id: rowId,
                trnLtrLang: lang,
            });
            if (res.length) {
                return res[0];
            } else {
                return null;
            }
        },
        async searchTranslate(rowId) {
            const res = await this.translate.search({ row_id: rowId });
            if (res.length) {
                return res[0];
            } else {
                return null;
            }
        },
        async searchTranslateLsnRowId(lnsRowId, lang) {
            const res = await this.translate.search({
                row_id: lnsRowId,
                trnLtrLang: lang,
            });
            if (res.length) {
                return res[0];
            } else {
                return null;
            }
        },
        async searchLesson(rowId) {
            const res = await this.lesson.search({ row_id: rowId });
            if (res.length) {
                return res[0];
            } else {
                return null;
            }
        },
        async searchCategory(rowId) {
            const res = await this.category.search({ row_id: rowId });
            if (res.length) {
                return res[0];
            } else {
                return null;
            }
        },
        async isCategoryPublished(rowId) {
            const resCat = await this.searchCategory(rowId);
            if (!resCat) return true;
            if (resCat.trnCatPublish) {
                if (resCat.trnCatId)
                    return await this.isCategoryPublished(resCat.trnCatId);
                else return true;
            }
            return false;
        },
        formatContent(suggestion, translate) {
            suggestion.content = this.higlightedContent(
                translate.trnLtrRawContent
            );
            suggestion.title = this.higlightedContent(translate.trnLtrTitle);
            suggestion.type = "simplicite";
            if (suggestion.content.length > 1000)
                suggestion.content = this.truncateContent(
                    suggestion.content,
                    this.contentMaxLength
                );
            this.suggestions.push(suggestion);
        },
        higlightedContent(content) {
            return content.replaceAll(
                this.inputValue,
                "<em>" + this.inputValue + "</em>"
            );
        },
        truncateContent(content, index) {
            if (
                content[index] === " " ||
                content[index] === "." ||
                content[index] === "?" ||
                content[index] === ";"
            ) {
                return content.substring(0, index) + " [...]";
            } else {
                return this.truncateContent(content, index + 1);
            }
        },
    },
};
</script>

<style lang="sass" scoped></style>
