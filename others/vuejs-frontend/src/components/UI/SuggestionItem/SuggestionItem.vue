<template>
  <div class="result-item" @click="suggestionSelected">
    <div class="result-header">
      <div class="result-title" v-html="suggestion.title"/>
      <div class="result-category">
        {{suggestion.path}}
      </div>
    </div>
    <div class="result-body">
      <MaterialIcon :type="suggestion.type"></MaterialIcon>
      <div class="result-text">
        <div v-html="suggestion.content"></div>
      </div>
    </div>
  </div>
</template>

<script>
import MaterialIcon from './MaterialIcon.vue';

export default {
  name: "SuggestionItem",
  props:{
    suggestion: Object,
  },
  components: {
    MaterialIcon
  },
  methods: {
    suggestionSelected: function() {
      this.$emit('suggestionItemSelected', this.suggestion);
    },
  }
}
</script>

<style lang="sass" scoped>
.result-item
  //border:solid black;
  color: black
  display: flex
  flex-direction: column
  width: 100%
  padding: .5rem
  height: 100%
  min-height: 10em
  &:hover
    background-color: #EDF3FA
    border-radius: .3rem
    cursor: pointer
  & :deep(em)
    background-color: yellow
    font-weight: bold
    font-style: normal
  & :deep(input)
    visibility: hidden

.result-header
  color: black
  display: flex
  justify-content: space-between
  padding-bottom: .5rem

.result-category
  padding: 0.3rem
  color: white
  background-color: grey
  border-radius: .3rem
  font-size: .8rem
  font-weight: lighter
  min-width: 12em
  text-align: center
  align-content: center

.result-title
  font-weight: bold
  font-size: 1.2rem

.result-body
  color: black
  display: flex
  height: 100%
  font-size: 1rem
  flex-grow: 1
 

.result-text
  color: black
  border-left: .10rem solid #ccc
  padding-left: .5rem
  flex-grow: 1
  line-height: 1.3rem
  & :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6)
    font-size: 1rem
    font-weight: normal


</style>