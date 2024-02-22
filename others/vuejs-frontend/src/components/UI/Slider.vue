<template>
  <div class="slider" @mouseover="controlsVisible = true" @mouseout="controlsVisible = false">
      <transition :name="transitionName" class="slider__image-wrapper" v-for="(slide, index) in slides" :key="index">
        <img v-show="index === currentImageIndex" :src="slide.filesrc" :alt="slide.filename" @click="$store.uiStore.displayLightBox(slide.filesrc)"/>
      </transition>
    <button class="slider__control slider__previous" v-if="controls" :class="controlsVisible && slides.length > 1 ? 'visible' : ''" @click.prevent="previous"><i class="material-icons">keyboard_arrow_left</i></button>
    <button class="slider__control slider__next" v-if="controls" :class="controlsVisible && slides.length > 1 ? 'visible' : ''" @click.prevent="next"><i class="material-icons">keyboard_arrow_right</i></button>
    <div class="slider__pagination" v-if="pagination && slides.length > 1">
      <button v-for="n in slides.length" :key="n" @click="goToImage(n-1)" :class="[n-1 === currentImageIndex ? 'active' : '']"/>
    </div>
  </div>
</template>

<script>
import { useUiStore } from '@/stores/ui';
  export default {
    name: "Slider",
    props: {
      slides: {
        type: Array,
        required: false,
        default: () => [],
        note: 'The slides in the carousel',
      },
      controls: {
        type: Boolean,
        required: false,
        default: true,
        note: 'Display the control elements of the slider',
      },
      pagination: {
        type: Boolean,
        required: false,
        default: true,
        note: 'Display the pagination at the bottom',
      },
    },
    setup() {
            return {
                uiStore: useUiStore(),
            }
        },
    data: () => ({
      currentImageIndex: 0,
      direction: null,
      controlsVisible: false
    }),
    methods: {
      next() {
        // Check grafikart vuejs slider video if there is a problem on loading, because maybe the there is a need to check this part of the code
        this.direction = 'right';
        if (this.currentImageIndex === this.slides.length - 1) this.currentImageIndex = 0;
        else this.currentImageIndex++;
      },
      previous() {
        this.direction = 'left';
        if (this.currentImageIndex === 0) this.currentImageIndex = this.slides.length - 1;
        else this.currentImageIndex--;
      },
      goToImage(indexToGo){
        if (typeof indexToGo === 'string') indexToGo = this.slides.findIndex(slide => slide.filename === indexToGo);
        if (indexToGo > this.currentImageIndex) this.direction = 'right';
        else this.direction = 'left';
        if (indexToGo > this.slides.length-1) this.currentImageIndex = 0;
        else if (indexToGo < 0) this.currentImageIndex = this.slides.length-1;
        else this.currentImageIndex = indexToGo;
      },
      displayFullScreenImage(imageSrc) {
        this.uiStore.displayLightBox(imageSrc);
      }
    },
    computed: {
      transitionName(){
        return 'slide-' + this.direction
      }
    },
  }
</script>

<style lang="sass" scoped>
@import "../../assets/sass/variables"
.slider
  overflow-y: hidden
  position: relative
  width: 100%
  height: 100%
  overflow-x: hidden
  img
    width: 100%
    height: 100%
    max-width: 100%
    max-height: 100%
    object-fit: contain
    &:hover
      cursor: pointer
  .slider__control
    opacity: 0
    transition: $slider-duration-control-apparition all
    outline: none
    border: none
    border-radius: 100%
    height: $slider-control-size
    width: $slider-control-size
    background-color: transparentize(black, 0.5)
    color: white
    display: flex
    justify-content: center
    align-items: center
  .slider__previous, .slider__next
    position: absolute
    top: 50%
    margin-top: -$slider-control-size/2
    &.visible
      opacity: 1
  .slider__next
    right: 10px
  .slider__previous
    left: 10px

    &.visible
      opacity: 1
  &__pagination
    position: absolute
    bottom: 10px
    left: 0
    right: 0
    text-align: center
    button
      display: inline-block
      border: transparent 2px solid
      outline: none
      width: $slider-pagination-size
      height: $slider-pagination-size
      border-radius: $slider-pagination-size
      margin: $slider-pagination-margin
      background-color: $slider-pagination-inactive-color
      &.active
        background-color: $slider-pagination-active-color
        border: solid 2px black

.slide-right-enter-active
  animation: slideRightIn $slider-duration-transition

.slide-right-leave-active
  animation: slideRightOut $slider-duration-transition
  position: absolute
  top: 0
  left: 0
  right: 0
  bottom: 0

@keyframes slideRightIn
  from
    transform: translateX(100%)
  to
    transform: translateX(0)

@keyframes slideRightOut
  from
    transform: translateX(0)
  to
    transform: translateX(-100%)

.slide-left-enter-active
  animation: slideLeftIn $slider-duration-transition

.slide-left-leave-active
  animation: slideLeftOut $slider-duration-transition
  position: absolute
  top: 0
  left: 0
  right: 0
  bottom: 0

@keyframes slideLeftIn
  from
    transform: translateX(-100%)
  to
    transform: translateX(0)

@keyframes slideLeftOut
  from
    transform: translateX(0)
  to
    transform: translateX(100%)
</style>
