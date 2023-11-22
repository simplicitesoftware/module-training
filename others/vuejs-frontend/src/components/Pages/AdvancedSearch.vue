<template>
  <div class="wrapper">
    <div class="grid">
      <div class="grid_item">
        <h1>Search</h1>
        <div class="content">
          <div class="search_container">
            <div class="search_bar">
              <input class="search" @input="search" type="search" placeholder="search" v-model="query">
              <Tooltip text="
                Use these notations to improve your search:
                + signifies AND operation
                | signifies OR operation
                - negates a single token
                &quot; wraps a number of tokens to signify a phrase for searching
                * at the end of a term signifies a prefix query
                ( and ) signify precedence
                ~N after a word signifies edit distance (fuzziness)
                ~N after a phrase signifies slop amount
              ">
                <span class="material-icons help_logo">help</span>
              </Tooltip>
            </div>
            <div class="button_layout">
              <button class="filter" v-bind:class="{active: documentationFilter}" type="button" @click="toggleDocumentationFilter">Documentation</button>
              <button class="filter" v-bind:class="{active: communityFilter}" type="button" @click="toggleCommunityFilter">Community</button>
            </div>
          </div>
          <Spinner class="spinner" v-if="fetchingResults"/>
          <div class="result_container" v-else-if="suggestions.length > 0">
            <div class="item"  v-for="suggestion in suggestions || []" :key="suggestion.id">
              <AdvancedSuggestionItem :suggestion="suggestion" />
            </div>
          </div>
          <div v-else class="no_results">No result</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters } from "vuex";
import s from "../../shared"
import AdvancedSuggestionItem from "../UI/SuggestionItem/AdvancedSuggestionItem.vue";
import Tooltip from "../UI/Tooltip.vue";
import Spinner from "../UI/Spinner";

export default {
  components: { AdvancedSuggestionItem, Spinner, Tooltip },
  name: "AdvancedSearch",
  data: () => ({
    query: "",
    suggestions: [],
    documentationFilter: false,
    communityFilter: false,
    fetchingResults: false
  }),
  async created() {
    const initialQuery = this.$router.currentRoute.params.query;
    if(initialQuery) { 
      this.query = initialQuery;
      await this.search()
    }
  },
  computed: {
    ...mapState({
      lesson: state => state.lesson.lesson,
      lessonImages: state => state.lesson.lessonImages,
    }),
    ...mapGetters({
      lang: 'ui/lang'
    }),
    getFilters: function() {
      const filters = [];
      if(this.documentationFilter) filters.push("documentation");
      if(this.communityFilter) filters.push("discourse");
      return filters;
    }
  },
  methods: {
    async search() {
      if(this.query) {
        this.fetchingResults = true;
        this.suggestions = await s.callSearchService(this.$smp.parameters.url, this.$smp.getBearerTokenHeader(), this.query, this.lang, this.getFilters);
        this.fetchingResults = false;
      } else {
        this.suggestions = []
      }
    },
    resetFilters(filter) {
      if(filter !== "documentation") {
        this.documentationFilter = false;
      }
      if(filter !== "community") {
        this.communityFilter = false;
      }
    },
    async toggleDocumentationFilter() {
      this.documentationFilter = !this.documentationFilter;
      this.resetFilters("documentation");
      await this.search();
    },
    async toggleCommunityFilter() {
      this.communityFilter = !this.communityFilter;
      this.resetFilters("community");
      await this.search();
    }
  },
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
                &:hover
                  cursor: pointer
                  background: #ccdefc
                  border-radius: 5px
        .result_container
          .item
            padding: 10px 10px 10px 0

            
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