<!-- eslint-disable no-debugger -->
<template lang="html">
  <div>
    <div v-for="suggestion in suggestions || []" :key="suggestion.row_id" >
      <SuggestionItem :suggestion="suggestion" :inputValue="inputValue" @suggestionItemSelected="suggestionSelected"/>
    </div>
  </div>
</template>

<script>
import SuggestionItem from "./SuggestionItem.vue";
import {mapGetters} from "vuex";

export default {
  name: "SimpliciteSuggestions",
  components: {
    SuggestionItem
  },
  props:{
    simpliciteHits: Array,
    inputValue: {
      type: String,
      default: "",
    },
  },
  data: function() {
    return {
      suggestions: [],
      contentMaxLength: 1000
    }
  },
  computed: {
    ...mapGetters({
      lang: 'ui/lang',
    }),
  },
  // async operation is made on component creation
  async created() {
    const obj = this.$smp.getBusinessObject("TrnLsnTranslate");
    for(const hit of this.simpliciteHits) {
      const suggestion = new Object();
      suggestion.path = hit.key;
      await this.setSuggestionContent(obj, suggestion, hit.row_id, this.lang);
      this.suggestions.push(suggestion);
    }
  },
  methods:{
    suggestionSelected(suggestion){
      this.$emit('suggestionSelected', suggestion);
    },
    async setSuggestionContent(obj, suggestion, rowId, lang) {
      const res = await obj.search({trnLtrLsnId: rowId, trnLtrLang: lang});
      if(res.length) {
        suggestion.content = res[0].trnLtrRawContent;
        suggestion.title = res[0].trnLtrTitle;
        if(suggestion.content.length > 1000) suggestion.content = this.truncateContent(suggestion.content, this.contentMaxLength);
      } else { // if no content set in language, look for "ANY" content
        await this.setSuggestionContent(obj, suggestion, rowId, "ANY");
      }
      
    },
    truncateContent(content, index) {
      if(content[index] === " " || content[index] === "." || content[index] === "?" || content[index] === ";") {
        return content.substring(0, index) + " [...]";
      } else {
        return this.truncateContent(content, index + 1);
      }
    }
  },
}

</script>
  
<style lang="sass" scoped>
  
</style>
  