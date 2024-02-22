<template>
  <div class="no-content">
    <h1>There is no content associated to the tag(s)</h1>
  </div>
</template>

<script>
  import {mapState} from 'pinia';
import { useUiStore } from '@/stores/ui';
  export default {
    name: 'TagNoContent',
    computed: {
      ...mapState(useUiStore, ['isSortedByTag']),
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
