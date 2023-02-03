<template>
  <div class="no-content">
    <h1>There is no content associated to the tag(s)</h1>
  </div>
</template>

<script>
  import {mapGetters} from 'vuex';
  export default {
    name: 'TagNoContent',
    computed: {
      ...mapGetters({
        isSortedByTag: "ui/isSortedByTag",
      })
    },
    mounted() {
      if(!this.isSortedByTag) {
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
