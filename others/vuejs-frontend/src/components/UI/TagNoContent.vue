<template>
  <div class="no-content">
    <h1>There is no content associated to the tag(s)</h1>
  </div>
</template>

<script>
  import {mapState, mapGetters} from 'vuex';
  export default {
    

    name: 'TagNoContent',
    computed: {
      ...mapState({
        tree: state => state.tree.tree,
      }),
      ...mapGetters({
        isSortedByTag: "ui/isSortedByTag",
      })
    },
    beforeMount() {
      // before component rendering, check if there is there is no content and tags selected. if true then go back to home page
      if(this.tree.length === 0 && !this.isSortedByTag) {
        this.$router.push("/");
      }
    }
    
  }
</script>

<style lang="sass" scoped>
h1
  color: black

.no-content
  height: 80vh
  width: 100%
  display: flex
  justify-content: center
  align-items: center
</style>
