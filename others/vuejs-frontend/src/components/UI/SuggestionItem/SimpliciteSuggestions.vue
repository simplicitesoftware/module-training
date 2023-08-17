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
import shared from "../../../shared";

export default {
    name: "SimpliciteSuggestions",
    components: {
        SuggestionItem,
    },
    props: {
        simpliciteHits: Array,
        contentMaxLength: Number,
        inputValue: {
            type: String,
            default: "",
        },
    },
    watch: {
        simpliciteHits: async function () {
			this.suggestions = await this.createSuggestions();
        },
    },
    data: function () {
        return {
            suggestions: [],
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
        this.truncateContent = shared.truncateContent;
        this.highlightedContent = shared.highlightedContent;
		this.suggestions = await this.createSuggestions();
    },
    methods: {
        suggestionSelected(suggestion) {
            this.$emit("suggestionSelected", suggestion);
        },
        async createSuggestions() {
			let newSuggestions = [];
            for (
                let i = 0;
                newSuggestions.length < this.suggestionMaxLength &&
                this.simpliciteHits.length > i;
                i++
            ) {
                const suggestion = await this.getSuggestion(this.simpliciteHits[i].row_id);
                if(Object.keys(suggestion).length > 0) {
                    newSuggestions.push(suggestion);
                }
            }
			return newSuggestions;
        },
        async getSuggestion(translateRowId) {
            let suggestion = new Object();
            let translateRes = await this.searchTranslate(translateRowId);
            if (translateRes) {
                if (translateRes.trnLtrLang === this.lang) {
                    const lessonRes = await this.searchLesson(
                        translateRes.trnLtrLsnId
                    );
                    if (lessonRes) {
                        suggestion = await this.validateAndSetSuggestion(
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
                            suggestion = await this.validateAndSetSuggestion(
                                suggestion,
                                lessonRes,
                                translateRes
                            );
                        }
                    }
                }
            }
            return suggestion;
        },
        async validateAndSetSuggestion(suggestion, lessonRes, translateRes) {
            
            const catPublished = await this.isCategoryPublished(
                lessonRes.trnLsnCatId,
                true
            );
            if (catPublished) {
                if (translateRes.trnLtrRawContent && lessonRes.trnLsnPublish) {
                    this.formatContent(suggestion, translateRes);
                    suggestion.path = lessonRes.trnLsnFrontPath;
                    suggestion.type = "simplicite";
                    suggestion.row_id = translateRes.row_id;
                }
            }
            return suggestion;
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
            suggestion.content = this.highlightedContent(
                translate.trnLtrRawContent,
                this.inputValue
            );
            suggestion.title = this.highlightedContent(
                translate.trnLtrTitle,
                this.inputValue
            );
            suggestion.type = "simplicite";
            if (suggestion.content.length > this.contentMaxLength)
                suggestion.content = this.truncateContent(
                    suggestion.content,
                    this.contentMaxLength
                );
        },
    },
};
</script>

<style lang="sass" scoped></style>
