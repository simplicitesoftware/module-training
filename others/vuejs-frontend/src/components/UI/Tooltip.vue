<template>
  <div class="tooltip-wrapper">
    <slot />
    <pre class="tooltip__text">{{ text }}</pre>
  </div>
</template>

<script>
export default {
  props: {
    text: {
      type: String,
      required: true
    }
  }
}
</script>

<style lang="sass" scoped>
.tooltip-wrapper
  display: inline-block
  position: relative
  padding-top: 3px
  &:hover
    .tooltip__text
      visibility: visible
      opacity: 1
  .tooltip__text
    position: absolute
    z-index: 1
    visibility: hidden
    padding-right: 600px
    opacity: 0
    max-width: 20%
    transition: opacity 1s
    
    color: #ffffff
    text-align: left
    border-radius: 2px
    
    background: #5e5d5d
    
    margin-inline-start: -300px

</style>