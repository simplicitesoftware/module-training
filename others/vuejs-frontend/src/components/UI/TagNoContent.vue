<template>
  <div class="no-content">
    <h1>There is no content associated to the tag(s)</h1>
  </div>
</template>

<script>
  export default {
    name: 'TagNoContent',
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
