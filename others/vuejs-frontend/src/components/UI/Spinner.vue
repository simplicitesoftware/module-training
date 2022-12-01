<!--https://codeburst.io/how-to-create-a-simple-css-loading-spinner-make-it-accessible-e5c83c2e464c-->
<template>
  <div class="spinner"></div>
</template>

<script>
  export default {
    name: "Spinner"
  };
</script>

<style lang="sass" scoped>
@keyframes spinner
  0%
    transform: translate3d(-50%, -50%, 0) rotate(0deg)

  100%
    transform: translate3d(-50%, -50%, 0) rotate(360deg)

.spinner
  // The height here is just for demo purposes
  height: 100%
  opacity: 1
  position: relative
  transition: opacity linear 0.1s
  &::before
    animation: 2s linear infinite spinner
    border: solid 3px #eee
    border-bottom-color: #EF6565
    border-radius: 50%
    content: ""
    height: 100px
    left: 50%
    opacity: inherit
    position: absolute
    top: 50%
    transform: translate3d(-50%, -50%, 0)
    transform-origin: center
    width: 100px
    will-change: transform
</style>
