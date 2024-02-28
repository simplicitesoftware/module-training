<template>
  <a :href="getSuggestionPath" class="result-item" @click="onClick($event)">
    <div class="result-header">
      <MaterialIcon :type="suggestion.type"></MaterialIcon>
      <div class="result-title" v-html="suggestion.title" />
      <div class="result-category">
        {{ suggestion.path }}
      </div>
    </div>
    <div class="result-body">
      <div class="result-text">
        <div v-html="suggestion.content"></div>
      </div>
    </div>
  </a>
</template>

<script>
import MaterialIcon from './MaterialIcon.vue';

export default {
  name: "SuggestionItem",
  props: {
    suggestion: Object,
  },
  components: {
    MaterialIcon
  },
  methods: {
    onClick(event) {
      if(event.type === "click") {
        if (this.suggestion.type === "lesson") {
          event.preventDefault();
          this.$router.push('/lesson' + this.suggestion.path).catch(err => console.error(err));
        }
      }
      
    }
  },
  computed: {
    getSuggestionPath() {
      if (this.suggestion.type === "discourse") {
        return this.suggestion.url;
      } else if (this.suggestion.type === "lesson") {
        return this.$smp.parameters.url + "/lesson" + this.suggestion.path;
      }
      return "";
    }
  },
}
</script>

<style lang="sass" scoped>
.result-item
  text-decoration: none
  color: black
  border-bottom: solid 1px #E0E0E0
  padding: 5px 5px 10px 5px 
  display: flex
  flex-direction: column
    
  &:hover
    cursor: pointer
    border: solid 1px #E0E0E0
    border-radius: 5px
    margin: -1px -1px 0 -1px
    box-shadow: rgba(0, 0, 0, 0.35) 0px 5px 15px
  & :deep(em)
    background-color: yellow,
    font-weight: bold
    font-style: normal
  
  & :deep(input)
    visibility: hidden
  
  & :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6)
    font-size: 1rem,
    font-weight: normal
  
      
  
  .result-header
    display: flex
    flex-direction: row
    align-items: center
    .result-title
      font-size: 30px
    .result-category
      margin-left: auto
      color: #696969       

</style>