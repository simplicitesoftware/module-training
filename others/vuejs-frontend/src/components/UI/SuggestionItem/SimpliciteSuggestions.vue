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
      keyRegex: /(\*|[A-Za-z]+) (.*)/,
      frontPathRegex: /[A-Za-z- _]* (.+)/
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
    const lesson = this.$smp.getBusinessObject("TrnLesson");
    for(let i = 0; this.suggestions.length < this.suggestionMaxLength && this.simpliciteHits.length > i; i++) {
        await this.processHit(translate, lesson, this.simpliciteHits[i], this.lang);
    }    
  },
  methods:{
    suggestionSelected(suggestion){
      this.$emit('suggestionSelected', suggestion);
    },
    async processHit(translate, lesson, hit, lang) {
        const pathMatch = hit.key.match(this.keyRegex);
        if(!pathMatch) {
            console.log(`Unable to parse path ${hit.key}`);
            return;
        }
        if(pathMatch[1] !== "*") {
            this.setSuggestion(pathMatch[2], translate, lesson, lang, hit.row_id);
        }
    },
    async setSuggestion(path, translate, lesson, lang, translateRowId) {
        const suggestion = new Object();
        const contentRes = await translate.search({row_id: translateRowId, trnLtrLang: lang});
        if(contentRes.length) {
            const lessonRes = await lesson.search({row_id: contentRes[0].trnLtrLsnId});
            if(lessonRes.length) {
                suggestion.path = lessonRes[0].trnLsnFrontPath;
            }
            if(contentRes[0].trnLtrRawContent) {
                this.formatContent(suggestion, contentRes[0]);
            }  else { // if no content set in language, look for "ANY" content
                const anyTranslate = await translate.search({trnLtrLsnId__trnLsnPath: path, trnLtrLang: "ANY"});
                if(anyTranslate.length) {
                    this.formatContent(suggestion, anyTranslate[0]);
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
  