<template>
  <div class="center-content">
    <div class="empty-content">
      <img class="empty-image" src="../../../public/empty.png" alt="Empty Content">
      <p>There is no content for this element</p>
    </div>
  </div>
</template>

<script>
  export default {
    name: "EmptyContent"
  };
</script>

<style lang="sass" scoped>
@import "../../assets/sass/variables"

.center-content
  display: flex
  justify-content: center
  align-items: center
  width: 100%
  height: 100%

.empty-content
  width: 50%
  height: 50%
  margin: auto
  text-align: center

.empty-image
  max-width: 100%
  max-height: 100%
  margin-bottom: 1em

</style>
