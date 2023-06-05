<template lang="html">
	<div>
		<div v-for="hit in formatHits || []" :key="hit._id" >
			<SuggestionItem :suggestion="hit" :inputValue="inputValue" @suggestionItemSelected="suggestionSelected"/>
		</div>
	</div>
</template>

<script>
import {mapGetters} from "vuex";
import SuggestionItem from "./SuggestionItem.vue";

export default {
	name: "ElasticSuggestions",
	components: {
		SuggestionItem
	},
	data: function() {
		return {
			contentMaxLength: 800
		}
	},
	props:{
		elasticHits: Array,
		inputValue: String
	},
	computed:{
		...mapGetters({
			langEsFormat: 'ui/langEsFormat'
		}),
		formatHits() {
			const suggestions = [];
			for(const hit of this.elasticHits) {
				const title = this.getTitle(hit, this.langEsFormat);
				const content = this.getHighlightContent(hit, this.langEsFormat);
				suggestions.push({
					title,
					path: hit._source.path,
					content,
					cat: hit._source.catPath,
					key: hit._id,
				})
			}
			return suggestions;
		},
	},
	methods:{
		suggestionSelected: function(suggestion){
			this.$emit('suggestionSelected', suggestion);
		},
		getHighlightContent: function(hit, lang) {
			if(hit.highlight["raw_content_" + lang]) {
				return this.stringifyHighlightedContent(hit.highlight["raw_content_" + lang]);
			} else if(hit._source["raw_content_" + lang]) {
				return hit._source["raw_content_" + lang];
			} else if (lang !== "any"){
				return this.getHighlightContent(hit, "any");
			}
			return "";
		},
		getTitle(hit, lang) {
			if(hit.highlight["title_" + lang]) {
				return hit.highlight["title_" + lang][0];
			}
			else if(hit._source["title_" + lang]) {
				return hit._source["title_" + lang];
			} else if(lang!=="any") {
				return this.getTitle(hit, "any");
			}
		},
		stringifyHighlightedContent(highlights) {
			let content = "";
			for(const h of highlights) {
				// prevents too big suggestions
				if(content.length > this.contentMaxLength) return content
				content += h + " [...]<br>";
			}
			return content;
		},
	},
}

</script>

<style lang="sass" scoped>

</style>
