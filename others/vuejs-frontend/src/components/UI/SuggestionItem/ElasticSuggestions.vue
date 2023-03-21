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
  props:{
      elasticHits: Array,
      inputValue: {
        type: String,
        default: "",
      },
  },
  computed:{
    ...mapGetters({
      langEsFormat: 'ui/langEsFormat'
    }),
    formatHits() {
      const suggestions = [];
      for(const hit of this.elasticHits) {
        const title = hit.highlight["title_" + this.langEsFormat] ? hit.highlight["title_" + this.langEsFormat][0] : hit._source["title_" + this.langEsFormat];
        const content = this.getHighlightContent(hit);
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
    getHighlightContent: function(hit) {
      if(hit.highlight["raw_content_" + this.langEsFormat]) {
        return this.stringifyHighlightedContent(hit.highlight["raw_content_" + this.langEsFormat]);
      } else {
        return hit._source["raw_content_" + this.langEsFormat];
      }
    },
    stringifyHighlightedContent(highlights) {
      let content = "";
      for(const h of highlights) {
        content += h + " [...]<br>";
      }
      return content;
    },
  },
}

</script>

<style lang="sass" scoped>

</style>
