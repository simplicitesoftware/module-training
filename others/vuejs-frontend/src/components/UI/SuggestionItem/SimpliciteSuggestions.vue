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
      contentMaxLength: 1000,
      suggestionMaxLength: 10,
      keyRegex: /(\*|[A-Za-z]+) (.*)/
    }
  },
  computed: {
    ...mapGetters({
      lang: 'ui/lang',
    }),
  },
  // async operation is made on component creation
  async created() {
    const translate = this.$smp.getBusinessObject("TrnLsnTranslate");
    for(let i = 0; i < this.suggestionMaxLength; i++) {
        const hit = this.simpliciteHits[i];
        const suggestion = new Object();
        suggestion.path = hit.key;
        await this.setSuggestionContent(translate, suggestion, hit.row_id, this.lang);
    }    
  },
  methods:{
    suggestionSelected(suggestion){
      this.$emit('suggestionSelected', suggestion);
    },
    async setSuggestionContent(translate, suggestion, rowId, lang) {
        const regexMatch = suggestion.path.match(this.keyRegex);
        console.log(regexMatch);
        if(regexMatch[1] !== "*") {
            const res = await translate.search({row_id: rowId, trnLtrLang: lang});
            const test45 = res;
            console.log(test45); 
            if(res.length) {
                if(res[0].trnLtrRawContent) {
                    this.formatContent(suggestion, res[0]);
                }  else { // if no content set in language, look for "ANY" content
                    const anyTranslate = await translate.search({trnLtrLsnId__trnLsnPath: regexMatch[2], trnLtrLang: "ANY"});
                    if(anyTranslate.length) {
                        this.formatContent(suggestion, anyTranslate[0]);
                    }
                }
            }
        }
    },
    formatContent(suggestion, translate) {
        suggestion.content = this.higlightedContent(translate.trnLtrRawContent);
        suggestion.title = this.higlightedContent(translate.trnLtrTitle);
        if(suggestion.content.length > 1000) suggestion.content = this.truncateContent(suggestion.content, this.contentMaxLength);
        this.suggestions.push(suggestion);
    },
    higlightedContent(content) {
        return content.replaceAll(this.inputValue, "<em>"+this.inputValue+"</em>");
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
  