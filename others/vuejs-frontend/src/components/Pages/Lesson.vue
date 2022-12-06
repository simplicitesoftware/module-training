<template>
  <div class="lesson" :class="[lesson.viz === 'LINEAR' ? 'linear' : '']" @click="changeVisualization">
    <div class="grid">
      <div class="grid-item lesson-block">
        <div v-if="lesson.row_id" class="lesson-wrapper">
          <ul class="breadcrumb">
            <li class="breadcrumb__item" v-for="(item, index) in breadCrumbItems" :key="index">
              <span>{{item.title}}</span>
              <span class="breadcrumb__divider" v-if="index !== breadCrumbItems.length-1">></span>
            </li>
          </ul>
          <ul class="tag">
            <li class="tag__list" v-for="(tag, index) in lessonTags" :key="index">
              <span class="tag__item">{{tag}}</span>
              <span class="breadcrumb__divider" v-if="index !== breadCrumbItems.length-1"></span>
            </li>
          </ul>
          <div class="lesson-html-content" v-if="lesson.html" v-html="lesson.html"
               v-highlightjs @click.prevent="handleClickOnLessonContent"></div>
          <EmptyContent v-else/>
        </div>
        <Spinner v-else/>
      </div>
      <div class="grid-item slider-block">
        <Slider v-if="lessonImages.length" :slides="lessonImages" ref="slider"/>
        <EmptyContent v-else/>
      </div>
      <div class="grid-item video-block">
        <div v-if="lesson" class="video-wrapper">
          <video v-if="videoUrl" class="video-player" controls muted poster="../../../public/media.svg"
                 :src="videoUrl" preload="none">
            Sorry, your browser doesn't support embedded videos.
          </video>
          <EmptyContent v-else/>
        </div>
        <Spinner v-else/>
      </div>
    </div>
  </div>
</template>

<script>
  /* eslint-disable no-console,no-unused-vars,no-undef */
  import Spinner from "../UI/Spinner";
  import EmptyContent from "../UI/EmptyContent";
  import Slider from "../UI/Slider";
  import {mapGetters, mapState} from "vuex";

  function getDocumentURL(vm) {
    return new Promise((resolve, reject) => {
      if (vm.lesson && vm.lesson.video){
        var obj = vm.$smp.getBusinessObject("TrnLsnTranslate");
        obj.get(
          vm.lesson.ltr_id
        ).then(item => {
          let url = obj.getFieldDocumentURL("trnLtrVideo", item)
          resolve(url);
        });
      }
    })
  }


  export default {
    name: "Lesson",
    components: {Slider, Spinner, EmptyContent},
    data: () => ({
      visualizationMode: 'tutorial',
      alreadyScrolledImages: [],
    }),
    asyncComputed: {
      async videoUrl() {
        if (this.lesson && this.lesson.video)
         return await getDocumentURL(this);
        else
          return false;
      }
    },
    computed: {
      ...mapState({
        lesson: state => state.lesson.lesson,
        lessonImages: state => state.lesson.lessonImages,
        lessonTags: state => state.lesson.lessonTags,
      }),
      ...mapGetters({
        breadCrumbItems: 'tree/breadCrumbItems',
        getLessonFromPath: 'tree/getLessonFromPath',
        isCategoryFromPath: 'tree/isCategoryFromPath'
      }),

    },
    methods: {
      handleClickOnLessonContent(event) {
        if (event && event.target && event.target.tagName === "A" && event.target.hasAttribute("href") && event.target.getAttribute("href").indexOf("#IMG_CLICK_") !== -1) {
          let imageName = event.target.getAttribute("href").split("#IMG_CLICK_")[1];
          this.$refs.slider.goToImage(imageName);
        }
      },
      addScrollListeners() {
        let potentialImages = [];
        document.querySelector(".lesson-block").addEventListener('scroll', (e) => {
          let imageName = null;
          let links = e.target.querySelectorAll("a");
          /* How this feature works : we go through all the a tags of the lesson-content element with #IMG_SCROLL_ & if their lower boundary is
          at a certain fixed point, we tell the slider to go to this image
           */
          for (let i = 0; i < links.length; i++) {
            if (links[i].hasAttribute("href") && links[i].getAttribute("href").includes("#IMG_SCROLL_")) {
              if (links[i].getBoundingClientRect().bottom < e.target.getBoundingClientRect().bottom) {
                imageName = links[i].getAttribute("href").split("#IMG_SCROLL_")[1];
                if (imageName && !this.alreadyScrolledImages.includes(imageName)) { // We add the imageName to the list
                  potentialImages.push(imageName);
                  this.alreadyScrolledImages.push(imageName);
                }
              }
            }
          }
          // On affiche la dernière image dans le carousel
          if (potentialImages.length) this.$refs.slider.goToImage(potentialImages[potentialImages.length - 1]);
        });
      },
      changeVisualization() {
        if (this.visualizationMode === 'tutorial') this.visualizationMode = 'linear'
        else this.visualizationMode = 'tutorial'
      },
      openLesson(lesson) {
        this.$store.dispatch("lesson/openLesson", {
          smp: this.$smp,
          lesson: lesson
        }).then(() => {
          if(this.$route.hash) {
            const id = this.$route.hash.replace('#', '');
            const el = document.getElementById(id);
            el.scrollIntoView();
          }
        })
      }
    },
    async created() {
      let splittedRoute = this.$router.currentRoute.path.split("lesson");
      let lessonPath = splittedRoute[1] ? splittedRoute[1] : "";
      if(lessonPath.includes(".md")){
        var mdLessonPath = lessonPath.split(".md");
        lessonPath = mdLessonPath[0];
      }
      let lesson = this.getLessonFromPath(lessonPath);
      console.log(lesson);
      if (!lesson || lesson.is_category) {
        // test if path is from a category
        const cat = this.isCategoryFromPath(lessonPath);
        
        if(cat) {
          console.log(cat);
          if(cat.lessons.length > 0) {
            lesson = this.getLessonFromPath(cat.lessons[0].path);
            this.openLesson(lesson);
          } else {
            await this.$router.push('/');
            this.$store.commit("tree/OPEN_NODE", cat.path);
          }
        } else {
          // if no lesson / cat
          await this.$router.push('/404');
        }
      }
      else {
        this.openLesson(lesson);
      }
    },
    mounted() {
      this.addScrollListeners();
    },
    beforeDestroy() {
      this.$store.dispatch('lesson/unsetLesson');
    },
    metaInfo() {
      let lesson = this.lesson;
      // Children can override the title.
      return {
        title: lesson.title
      }
    }
  };
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="sass" scoped>
@import "../../assets/sass/variables"
@import "../../assets/sass/mixins"
.lesson
  
  position: relative
.linear
  .video-block, .slider-block
    display: none
  .lesson-block
    grid-column: 1/3

pre
    padding-bottom: 0.7rem
table
  padding-bottom: 0.7rem


.grid
  position: absolute
  width: 100%
  height: 100%
  display: grid
  grid-template-columns: repeat(2, 50%)
  grid-template-rows: repeat(2, 50%)

.grid-item
  margin: 1em
  background: white
  border-radius: map-get($radius, regular)
  @include box-shadow
  overflow: auto

.lesson-block
  grid-column: 1
  grid-row: 1 / 3
  padding: 1em
  border-bottom: 1px solid #eee
  background-color: white
  padding-bottom: 1em
  .breadcrumb
    list-style-type: none
    display: flex
    flex-wrap: wrap
    padding: 0.75rem 1rem
    margin-bottom: none
    border-radius: 0.25rem
    border-bottom: 1px solid #eee
    &__item
      text-transform: uppercase
    &__divider
      margin: $breadcrumb-divider-margin
      text-transform: uppercase
  .lesson-wrapper
    @include fillParent()
  .tag
    display: flex
    flex-direction: row
    list-style: none
    padding-top: 0.5rem
    &__item
      background-color: #ddd
      border: none
      color: black
      padding: 8px 13px
      text-align: center
      text-decoration: none
      display: inline-block
      margin: 4px 2px
      border-radius: 10px


.slider-block
  grid-column: 2
  grid-row: 1

.video-block
  overflow-y: hidden
  grid-column: 2
  grid-row: 2
  .video-wrapper
    @include fillParent()
  .video-player
    @include fillParent()
    object-fit: contain

.lesson-html-content
  margin-top: 0.7rem
  line-height: 1.5
  font-size: 1rem
  @include flex-column-nowrap
  overflow: hidden
  /* ::v-deep is used instead of >>> because we are using sass. It is a deep selector to apply styles to the v-html content*/
  & ::v-deep
    :not(pre) code.hljs  // targets inline code
      display: inline
      padding: 3px
    table
      width: 100%
      padding: map_get($paddings, "large")
      border-collapse: collapse
      color: $table-color-text
      th
        font-size: map_get($title-sizes, "x-small")
        font-weight: bold
        background-color: $table-color-head-background
        padding: map_get($paddings, "medium")

      tr
        border-top: 1px solid #dee2e6
        &:nth-child(odd)
          background-color: $table-color-row-background
      th, td
        padding: map_get($paddings, "small")

    h1, h2, h3, h4, h5, h6
      font-weight: normal      
    h1
      font-size: map-get($title-sizes, 4)
    h2
      font-size: map-get($title-sizes, 5)
      color: $color-secondary
    h3
      font-size: map-get($title-sizes, 6)
      color: $color-secondary
    h4
      font-size: map-get($title-sizes, 6)
      color: $color-secondary
    h5
      font-size: map-get($title-sizes, 6)
      color: $color-secondary
    h6
      font-size: map-get($title-sizes, 6) - 0.1rem
      color: $color-secondary
    p
      text-align: justify
      padding-bottom: 0.6rem
      padding-top: 0.4rem

    .info, .success, .warning, .error
      border-radius: map-get($radius, regular)
      padding: 0.7rem
      margin: map-get($margins, x-small)
    .info
      background-color: $color-information
    .success
      background-color: $color-success
    .warning
      background-color: $color-warning
    .error
      background-color: $color-error

    blockquote > p::before
      content: '" '
    blockquote > p::after
      content: ' "'
    blockquote > p
      font-style: italic
    strong
      text-decoration: underline
    ol
      list-style-type: decimal
      padding-left: 25px //ol and ul require a certain amount of padding to display the style-type
      //The use of the reset style in app is still to keep though, because it doesn't provoke other issues
      padding-bottom: 0.4rem
      padding-top: 0.4rem


    ul
      list-style-type: disc
      padding-left: 25px
      padding-bottom: 0.4rem
      padding-top: 0.4rem

    img
      width: 100%
      max-width: 100%
      max-height: 250px
      object-fit: contain
      margin: 5px auto

    a
      color: #007bff
      text-decoration: none
      background-color: transparent
      &:hover
        color: #0062ff
        text-decoration: underline
</style>
