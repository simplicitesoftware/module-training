<template>
  <transition name="light-box">
    <div class="light-box" v-show="isLightBoxVisible">
      <div class="light-box__overlay" @click="$store.dispatch('ui/hideLightBox')"></div>
      <img class="light-box__image" :src="lightBoxImageSrc" alt="light-box image"/>
    </div>
  </transition>
</template>

<script>
  import {mapState} from "vuex";

  export default {
    name: "LightBox",
    computed: {
      ...mapState({
        lightBoxImageSrc: state => state.ui.lightBoxImageSrc,
        isLightBoxVisible: state => state.ui.isLightBoxVisible
      }),
    },
  }
</script>

<style lang="sass" scoped>
@import "../../assets/sass/variables"
.light-box
  position: fixed
  width: 100%
  height: 100vh
  z-index: $light-box-z-index
  &__overlay
    width: 100%
    height: 100%
    background-color: $light-box-overlay-background
    &:hover
      cursor: pointer
  &__image
    border-radius: map-get($radius, regular)
    max-width: 80%
    max-height: 80%
    position: absolute
    top: 10%
    left: 15%
.light-box-enter-active
  animation: lightBoxIn $light-box-duration-apparition
.light-box-enter-leave
  animation: lightBoxOut $light-box-duration-apparition
@keyframes lightBoxIn
  from
    opacity: 0
  to
    opacity: 1
@keyframes lightBoxOut
  from
    opacity: 1
  to
    opacity: 0
</style>
