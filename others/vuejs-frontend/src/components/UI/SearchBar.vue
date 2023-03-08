<template>
    <div id="SearchBar"  v-click-outside="hideSuggestions">
        <div class="searchElement">
          <input class="searchbar" @input="queryIndex" v-model="inputValue" type="text" name="" value="" :placeholder="searchbarPlaceHolder"/>
          <div @click="searchIconClick" class="searchbar-logo-container" :style="{[`background-color`]: `${themeValues.primaryColor}`}">
              <span class="material-icons searchbar-logo">
                  search
              </span>
          </div>
        </div>
        <div class="suggestionRelative">
          <div v-if="isSugOpen" class="result-list-container">
                <div class="" v-if="suggestions">
                  <div v-if="this.$SEARCH_TYPE=='elasticsearch'" class="">
                    <div v-for="suggestion in suggestions || []"
                      :key="suggestion._id"
                      @click="suggestionSelected(suggestion)"
                    >
                    <suggestion-item :inputValue="inputValue" :suggestion="suggestion" />
                  </div>
                  </div>
                  <div  v-if="this.$SEARCH_TYPE=='simplicite'" class="">
                    <div v-for="suggestion in (suggestions || []).map(s => ({
                          label: s.label,
                          value: s.value,
                          excerpt: s.value,
                          cat: s.key,
                          path: s.key,
                          key: s.row_id,
                          excerptHighlight: s.content
                      }))"
                      :key="suggestion.row_id"
                      @click="suggestionSelected(suggestion)"
                    >
                        <suggestion-item :inputValue="inputValue" :suggestion="suggestion" />
                    </div>
                  </div>
                </div>
                <div class="result-list-empty" v-else>
                  {{emptyResult}}
                </div>


          </div>
        </div>
    </div>
</template>


<script>

import SuggestionItem from "./SuggestionItem";
import {mapGetters} from "vuex";

export default {

  name: "SearchBar",
  components :{
    SuggestionItem
  },
  props: {
    themeValues: Object
  },
  data: function() {
    return {
      inputValue:'',
      searchUsed: false,
      searchFields : [
        {
          field: 'title',
          weight: '3'
        },
        {
          field: 'raw_content',
          weight: '2'
        }
      ],
      highlightFields: ["title", "raw_content"],
      hover: false,
      isSugOpen:false,
      queryInput:'',
      result:'',
      suggestions:null,
      sugsExist:false,
      timeout:null,
    }
  },
  computed:{
    ...mapGetters({
      lang: 'ui/lang',
      langEsFormat: 'ui/langEsFormat'
    }),
    getsearchFields : function() {
      return this.searchFields.map((f) => f.field+"_"+this.langEsFormat+"^"+f.weight);
    },
    gethighlightFields : function() {
      const obj = new Object();
      for(const f of this.highlightFields) {
        obj[f+"_"+this.langEsFormat] = {};
      } 
      return obj;
    },
    es_index : function(){
      return process.env.VUE_APP_ES_INDEX+"_"+this.lang.toLowerCase()
    },
    searchbarPlaceHolder : function(){
      return "FRA" == this.lang ? "Rechercher" : "Search"
    },
    emptyResult : function(){
      return "FRA" == this.lang ? "Aucun résultat de recherche." : "No results found."
    },
    ...mapGetters({
      getLessonFromPath: 'tree/getLessonFromPath',
    }),
  },
  methods: {
    hideSuggestions(){
      this.isSugOpen = false;
    },
    suggestionSelected(item){
      this.isSugOpen = false
      this.inputValue = ''
      const lsn = this.getLessonFromPath(item.path);
      if (lsn && !lsn.is_category) this.$router.push('/lesson' + item.path).catch(err => console.error(err))
      else this.$store.commit("tree/OPEN_NODE", item.path);
    },
    searchIconClick() {
      this.isSugOpen = true
    },
    valueSelected(val, event, item) {
      this.isSugOpen = false
      if(item !== undefined){
          this.$router.push('/lesson' + item.trnLsnFrontPath).catch(err => console.error(err));
      }
    },
    queryIndex(){

      clearTimeout(this.timeout);

      // Make a new timeout set to go off in 500ms (0.5 second)
      this.timeout = setTimeout(function () {
        if(this.inputValue == ''){
          this.isSugOpen = false
        }
        else{
          // console.log(this.inputValue)
          this.isSugOpen = true;
          if(this.$SEARCH_TYPE == "elasticsearch"){
            this.searchElasticSearch(this.inputValue)
          }
          else if(this.$SEARCH_TYPE == "simplicite"){
            this.searchSimplicite(this.inputValue)
          }
          else if(this.$SEARCH_TYPE == "community"){
            this.searchCommnuity(this.inputValue)
          }
        }
      }.bind(this), 500);
    },
    searchSimplicite(inputValue){

      var headers = new Headers();
      headers.append("Authorization", this.$smp.getBearerTokenHeader());
      headers.append("Content-Type", "application/json");

      var requestOptions = {
        method: 'GET',
        headers: headers,
        redirect: 'follow'
      };
      fetch(this.$smp.parameters.url+"/api/rest/?_indexsearch="+inputValue, requestOptions)
        .then(response => response.json())
        .then(async (json) => {
          var hits = json.filter(elem => elem.object === "TrnLesson" || elem.object === "TrnCategory");
          if(hits.length != 0){
            const obj = this.$smp.getBusinessObject("TrnLsnTranslate");
            for(const hit of hits) {
              const res = await obj.search({trnLtrLsnId: hit.row_id, trnLtrLang: this.lang});
              if(res.length) {
                hit.content = res[0].trnLtrRawContent;
                hit.label = res[0].trnLtrTitle;
              } else {
                const anyRes = await obj.search({trnLtrLsnId: hit.row_id, trnLtrLang: "ANY"});
                if(anyRes.length) {
                  hit.content = anyRes[0].trnLtrRawContent;
                  hit.label = anyRes[0].trnLtrTitle;
                }
              }
            }
            this.suggestions = hits
          }
          else{
            this.suggestions = null
          }
        })
        .catch(error => console.log('error', error));

    },
    searchElasticSearch(inputValue){

      if(inputValue == ''){
        this.isSugOpen = false
      }
      else{

        const myHeaders = new Headers();

        const test = this.$ES_CREDENTIALS;
        console.log(test);
        //const authent = btoa(this.$ES_CREDENTIALS);

        //myHeaders.append("Authorization", "Basic "+authent);
        myHeaders.append("Content-Type", "application/json");
        // eslint-disable-next-line no-unused-vars
        const esInstance = this.$ES_INSTANCE;
        myHeaders.append("Origin", this.$ES_INSTANCE);
 

        const json = {
          "query": {
              "multi_match": {
                  "type": "phrase_prefix",
                  "query": inputValue,
                  "fields": this.getsearchFields
              }
          },
          "highlight": {
              "fields": this.gethighlightFields,
              "fragment_size":500
          },
          "size": 10
        };
        const raw = JSON.stringify(
          json
        );

        var requestOptions = {
          method: 'POST',
          headers: myHeaders,
          body: raw,
          redirect: 'follow'
        };

        const searchUrl = this.$ES_INSTANCE+"/"+this.$ES_INDEX+"/_search";

        fetch(searchUrl, requestOptions)
        .then(response => response.json())
        .then(json => {
          var hits = json.hits.hits
          console.log(hits)
          if(hits.length != 0){
            this.suggestions = this.getSuggestion(hits); 
          }
          else{
            this.suggestions = null
          }
        })
        .catch(error => console.log('error', error));

      }
    },
    searchCommnuity(inputValue){
      console.log(inputValue)
      var myHeaders = new Headers();
      myHeaders.append("key", "1d6d13346f39ffa120b0c0c3afe5212c23ea71a5d3a76bc18d9013e7d1fc2f98");
      var requestOptions = {
        method: 'GET',
        headers: myHeaders,
        redirect: 'follow'
      };

      //fetch("https://community.simplicite.io/search.json?q=pouvoir créer un agenda", requestOptions)
      fetch("https://community.simplicite.io/posts.json", requestOptions)
        .then(response => response.json())
        .then(json => {
          var hits = json;
          console.log(hits)
        })
        .catch(error => console.log('error', error));
    },
    getSuggestion(hits) {
      const formatHits = [];
      for(const hit of hits) {
        formatHits.push({
          label: hit._source["title_" + this.langEsFormat],
          value: hit._source["title_" + this.langEsFormat],
          content: hit._source["raw_content_" + this.langEsFormat],
          path: hit._source.path,
          titleHighlight: hit.highlight["title_" + this.langEsFormat],
          excerptHighlight: hit.highlight["raw_content_" + this.langEsFormat],
          cat: hit._source.catPath,
          key: hit._id,
          source: hit._source
        })
      }
      return formatHits;
      
    }
  }
};

</script>

<style scoped lang="sass">
@import "../../assets/sass/variables"
@import "../../assets/sass/mixins"

#SearchBar
  //display: flex
  //border: 1px solid #ccc
  min-width: 40rem

.searchElement
  display: flex
  width: 50vw

.searchbar
  //height: 100%
  //width: 100%
  flex-basis: 100%
  padding-left: 10px
  font-size: 1rem

.searchbar-logo-container
  display: flex
  justify-content: center
  align-items: center
  flex-direction: column
  padding: .5rem

  &:hover
    background-color: transparentize(white, 0.9)
    cursor: pointer

.searchbar-logo


.suggestionRelative
  position: relative

.datasearch
  display: flex
  flex-direction: column

.result-list-container
  display: flex
  flex-direction: column
  border-top: 1.5px solid #ccc
  border-left: 1.5px solid #ccc
  border-right: 1.5px solid #ccc
  border-bottom: 1.5px solid #ccc
  background-color: #fafafa
  border-radius: 0 0 8px 8px
  position: absolute
  z-index: 1
  overflow: scroll
  max-height: 50rem
  width: 50vw
  //width: 50%
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19)

.result-container
  &:hover
    background-color: #EDF3FA
    border-radius: .3rem
    cursor: pointer

.result-list-empty
  color: black
  padding: .5rem

.result-title
  font-weight: bold
  font-size: 1.2rem


.result-url
  padding: .5rem 0 .5rem 0


.result-body
  height: 100%
  font-size: 1rem


.result-item
  //border:solid black;
  display: flex
  flex-direction: column
  width: 100%
  padding: .5rem
  height: 100%
  min-height: 10em



</style>
