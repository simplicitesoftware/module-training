<template>
  <div class="wrapper">
    <div class="grid">
      <div class="grid_item" @scroll="onScroll">
        <h1>Search</h1>
        <div class="content">
          <div class="search_container">
            <div class="search_bar">
              <input class="search" @input="  search(false)" type="search" placeholder="search" v-model="query">
              <span class="material-icons help_logo" title="
                Use these notations to improve your search:
                + signifies AND operation
                | signifies OR operation
                - negates a single token
                &quot; wraps a number of tokens to signify a phrase for searching
                * at the end of a term signifies a prefix query
                ( and ) signify precedence
                ~N after a word signifies edit distance (fuzziness)
                ~N after a phrase signifies slop amount
              ">help</span>
            </div>
            <div class="button_layout">
              <button class="filter" v-bind:class="{ active: documentationFilter }" type="button"
                @click="toggleDocumentationFilter">Documentation</button>
              <button class="filter" v-bind:class="{ active: communityFilter }" type="button"
                @click="toggleCommunityFilter">Community</button>
            </div>
          </div>
          <div class="result_container" v-if="suggestions.length > 0">
            <div class="search-info" v-if="searchInfo.totalHits">{{ searchInfo.totalHits }} results</div>
            <div class="item" v-for="suggestion in suggestions || []" :key="suggestion.id">
              <AdvancedSuggestionItem :suggestion="suggestion" />
            </div>
          </div>
          <div v-else class="no_results">No result</div>
          <Spinner v-if="fetchingResults && !allResults"></Spinner>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState} from "pinia";
import s from "../../shared"
import AdvancedSuggestionItem from "../UI/SuggestionItem/AdvancedSuggestionItem.vue";
import Spinner from "../UI/Spinner.vue";
import { useLessonStore } from "@/stores/lesson";
import { useUiStore } from '@/stores/ui';

export default {
  components: { AdvancedSuggestionItem, Spinner },
  name: "AdvancedSearch",
  data: () => ({
    query: "",
    suggestions: [],
    searchInfo: null,
    documentationFilter: false,
    communityFilter: false,
    fetchingResults: false,
    page: 0,
    allResults: false
  }),
  async created() {
    const initialQuery = this.$router.currentRoute.value.params.query;
    if (initialQuery) {
      this.query = initialQuery;
      await this.search()
    }
  },
  computed: {
    ...mapState(useLessonStore, ['lesson', 'lessonImages']),
    ...mapState(useUiStore, ['lang']),
    getFilters: function () {
      const filters = [];
      if (this.documentationFilter) filters.push("documentation");
      if (this.communityFilter) filters.push("discourse");
      return filters;
    }
  },
  methods: {
    async search() {
      this.page = 0;
      if (this.query) {
        this.fetchingResults = true;
        this.allResults = false;
        try{
          const res = await s.callSearchService(this.$smp.parameters.url, this.$smp.getBearerTokenHeader(), this.query, this.lang, this.getFilters, this.page);
          this.suggestions = res.results;
          this.searchInfo = new ResultInfo(res.search_info); 
          this.page += this.searchInfo.pageIncrement;
        }catch(err){
          console.log("error in queryIndex", err);
        }
        
       
        this.fetchingResults = false;
      } else {
        this.suggestions = [];
        this.fetchingResults = false;
      }
    },
    async searchScroll() {
      if (this.query) {
        this.fetchingResults = true;
        try{
          const res = await s.callSearchService(this.$smp.parameters.url, this.$smp.getBearerTokenHeader(), this.query, this.lang, this.getFilters, this.page);
          const fetchedSuggestions = res.results;
          if (fetchedSuggestions.length === 0) {
            this.allResults = true;
            this.suggestions = this.suggestions.concat(fetchedSuggestions);
          }
        }catch(err){
          console.log("error in queryIndex",);
        }
        this.page += this.searchInfo.pageIncrement;
      }
    },
    resetFilters(filter) {
      if (filter !== "documentation") {
        this.documentationFilter = false;
      }
      if (filter !== "community") {
        this.communityFilter = false;
      }
    },
    async toggleDocumentationFilter() {
      this.documentationFilter = !this.documentationFilter;
      this.resetFilters("documentation");
      this.page = 0;
      this.allResults = false;
      await this.search();
    },
    async toggleCommunityFilter() {
      this.communityFilter = !this.communityFilter;
      this.resetFilters("community");
      this.page = 0;
      this.allResults = false;
      await this.search();
    },
    async onScroll({ target: { scrollTop, clientHeight, scrollHeight } }) {
      if (scrollTop + clientHeight + 1 >= scrollHeight) {
        await this.searchScroll();
      }
    }
  },
}

class ResultInfo {
  constructor(info) {
    this.totalHits = info.total_hits;
    this.searchDuration = info.search_duration;
    this.searchType = info.search_type;
    this.pageIncrement = info.page_increment;
  }
}
</script>

<style lang="sass" scoped>
@import "../../assets/sass/variables"
@import "../../assets/sass/mixins"

.wrapper
  position: relative
  width: 100%
  height: 100%
  .grid
    position: absolute
    width: 100%
    height: 100%
    display: grid
    grid-template-columns: repeat(2, 50%)
    grid-template-rows: repeat(2, 50%)
    .grid_item
      grid-row: 1/3
      padding: 1em 0 0 25px
      border-bottom: 1px solid #eee
      background-color: #fff
      grid-column: 1/3
      margin: 1em
      background: white
      border-radius: 4px
      box-shadow: 0 0 9px 2px rgb(204, 204, 204)
      overflow: auto

      h1
        color: #5BC0DE
        border-bottom: solid 1px #E0E0E0
        margin: 0 0 20px 0
        padding: 0 0 10px 0
        font-weight: normal
        font-size: 36px
      .content
        width: 80%
        .spinner
          padding-top: 300px
        .search_container
          display: flex
          flex-direction: column
          padding-bottom: 10px
          align-content: center
          .search_bar
            display: flex
            flex-direction: row
            padding-bottom: 15px
            .search
              width: 100%
              font-size: 1.5rem
              border: 1px solid grey
              border-radius: 3px
              margin-right: 20px               

            .help_logo
              padding-top: 2px
              &:hover
                cursor: pointer
        .result_container
          .item
            padding: 10px 10px 10px 0
          .search-info
            font-size: 1rem
            font-style: italic
            
        .button_layout
          padding: 0 0 8px 0
          border-bottom: 1px solid grey
          .filter
            border: 2px solid #E0E0E0
            font-size: 1.2rem
            margin: -1px
          .active
            background: #cfe0fa
        button
          &:hover
            cursor: pointer
            box-shadow: rgba(0, 0, 0, 0.35) 0px 5px 15px


        
		
</style>