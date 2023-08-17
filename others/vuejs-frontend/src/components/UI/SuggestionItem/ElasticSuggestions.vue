<template lang="html">
    <div>
        <div v-for="hit in suggestions || []" :key="hit.key">
            <SuggestionItem
                :suggestion="hit"
                :inputValue="inputValue"
                @suggestionItemSelected="suggestionSelected"
            />
        </div>
    </div>
</template>

<script>
import { mapGetters } from "vuex";
import SuggestionItem from "./SuggestionItem.vue";
import shared from "../../../shared";

export default {
    name: "ElasticSuggestions",
    components: {
        SuggestionItem,
    },
    data: function () {
        return {
            suggestions: [],
			truncateContent: () => {},
			highlightedContent: () => {}
        };
    },
    props: {
        elasticHits: Array,
        inputValue: String,
		contentMaxLength: Number
    },
    created() {
		this.truncateContent = shared.truncateContent;
		this.highlightedContent = shared.highlightedContent;
        this.suggestions = this.formatHits();
    },
    watch: {
        elasticHits: function () {
            this.suggestions = this.formatHits();
        },
    },
    computed: {
        ...mapGetters({
            langEsFormat: "ui/langEsFormat",
        }),
    },
    methods: {
        suggestionSelected: function (suggestion) {
            this.$emit("suggestionSelected", suggestion);
        },
        formatHits() {
            const suggestions = [];
            for (const hit of this.elasticHits) {
                let suggestion;
                if (hit._source.type === "lesson") {
                    suggestion = this.createLessonSuggestion(hit);
                } else if (hit._source.type === "discourse") {
                    suggestion = this.createDiscourseSuggestion(hit);
                }
                suggestions.push(suggestion);
            }
            return suggestions;
        },
        createLessonSuggestion(hit) {
            return {
                title: this.getLessonTitle(hit, this.langEsFormat),
                type: "lesson",
                path: hit._source.path,
                content: this.getLessonHighlightContent(hit, this.langEsFormat),
                cat: hit._source.catPath,
                key: hit._id,
            };
        },
        createDiscourseSuggestion(hit) {
            return {
                title: this.getDiscourseTitle(hit),
                type: "discourse",
                path: hit._source.url,
                content: this.getDiscourseHighlightContent(hit),
                cat: hit._source.catPath,
                key: hit._id,
            };
        },
        getLessonHighlightContent(hit, lang) {
            if (hit.highlight["raw_content_" + lang]) {
                return this.stringifyHighlightedContent(
                    hit.highlight["raw_content_" + lang]
                );
            } else if (hit._source["raw_content_" + lang]) {
                return this.formatSourceContent(hit._source["raw_content_" + lang]);
            } else if (lang !== "any") {
                return this.getLessonHighlightContent(hit, "any");
            }
            return "";
        },
        getDiscourseHighlightContent(hit) {
            if (hit.highlight["posts.content"]) {
                return this.stringifyHighlightedContent(
                    hit.highlight["posts.content"]
                );
            } else if (hit._source["posts"]) {
				const posts = hit._source["posts"];
				let postString = "";
				for(const post of posts) {
					postString += post.content + " ";
				}	
                return this.formatSourceContent(postString);
            }
            return "";
        },
		formatSourceContent(content) {
			let highlightedContent = this.highlightedContent(content, this.inputValue);
			if(highlightedContent.length > this.contentMaxLength) {
				highlightedContent = this.truncateContent(highlightedContent, this.contentMaxLength);
			}
			return highlightedContent;
		},
        getLessonTitle(hit, lang) {
            if (hit.highlight["title_" + lang]) {
                return hit.highlight["title_" + lang][0];
            } else if (hit._source["title_" + lang]) {
                return hit._source["title_" + lang];
            } else if (lang !== "any") {
                return this.getLessonTitle(hit, "any");
            }
            return "";
        },
        getDiscourseTitle(hit) {
            if (hit.highlight["title"]) {
                return hit.highlight["title"][0];
            } else if (hit._source["title"]) {
				return this.highlightedContent(hit._source["title"], this.inputValue);
            }
            return "";
        },
        stringifyHighlightedContent(highlights) {
            let content = "";
            for (const h of highlights) {
                // prevents too big suggestions
                if (content.length > this.contentMaxLength) return content;
                content += h + " [...]<br>";
            }
            return content;
        },
    },
};
</script>

<style lang="sass" scoped></style>
