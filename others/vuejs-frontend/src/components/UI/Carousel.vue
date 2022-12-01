<!-- https://christianheilmann.com/2015/04/08/keeping-it-simple-coding-a-carousel/ -->
<!-- https://github.com/codepo8/simple-carousel/blob/master/carousel-images.html -->
<template>
  <div class="carousel">
    <div class="carousel__img-holder">
      <transition :duration="125">
        <img :src="currentImg.filesrc" alt="carousel image" :download="currentImg.filename"
             v-bind:key="currentImg.filename" @click="displayFullScreenImage(currentImg.filesrc)">
      </transition>
    </div>
    <div class="carousel__buttons">
      <button @click="navigate(-1)"> ◀</button>
      <p class="carousel__img-counter">{{(this.counter + 1)}} / {{this.images.length}}</p>
      <button @click="navigate(1)"> ▶</button>
    </div>
  </div>

</template>

<script>
  export default {
    name: "Carousel",
    props: {
      images: {
        type: Array,
        required: true,
        default: () => [],
        note: 'The slides in the carousel',
      },
    },
    data: () => ({
      counter: 0
    }),
    computed: {
      currentImg() {
        return this.images[this.counter];
      }
    },
    methods: {
      navigate(direction) {
        this.counter = (this.counter + direction) % this.images.length;
        this.counter = this.counter < 0 ? this.images.length - 1 : this.counter;
      },
      displayFile(filename) {
        for (let i = 0; i < this.images.length; i++) {
          if (this.images[i].filename === filename) {
            this.counter = i;
            break;
          }
        }
      },
      displayFullScreenImage(image) {
        this.$store.dispatch('ui/displayLightBox', image);
      }
    },
    mounted() {
      if (!this.images.length) {
        let buttons = document.querySelector(".carousel__buttons");
        buttons.style.visibility = "hidden";
      } else {
        let box = document.querySelector(".carousel");
        let buttons = document.querySelector(".carousel__buttons");
        box.addEventListener('mouseover', () => {
          buttons.style.visibility = "visible";
        });
        box.addEventListener('mouseout', e => {
          if (!e.relatedTarget.classList.contains('carousel__buttons')) {
            buttons.style.visibility = "hidden";
          }

        });
        buttons.addEventListener('mouseleave', () => {
          buttons.style.visibility = "hidden";
        });
      }
    }
  };
</script>

<style lang="sass" scoped>
@import "../../assets/sass/variables"
.carousel
  width: 100%
  height: 100%
  position: relative
  margin: 0
  overflow: hidden // avoid transition scrollbar

  &__buttons
    width: 100%
    height: 40px
    position: absolute
    bottom: 0
    padding: 5px 0
    text-align: center
    background: #eee
    z-index: 10
    visibility: hidden
    display: flex
    justify-content: center
    align-items: center
    button
      border: none

  &__img-counter
    height: 100%
    margin: 0 $img-counter-margin 0 $img-counter-margin

  &__img-holder
    &:hover
      cursor: pointer
    img
      max-width: 100%
      max-height: 100%

  /* Transition */
  .v-enter, .v-leave-to
    //transform: scale(0)
    opacity: 0

  .v-enter-to, .v-leave
    //transform: scale(1)
    opacity: 1

  .v-leave-active, .v-enter-active
    transition-duration: 0.2s

  .v-enter-active
    transition-delay: 0.2s

</style>
