<template>
  <header id="top-menu" :style="{background: `linear-gradient(to right, ${themeValues.primaryColor} 65%, ${themeValues.secondaryColor})`}">
    <div class="menu-icon" @click="toggleMenu">
      <i class="material-icons menu-icon__image">menu</i>
    </div>
    <div class="logo-warning" v-if="themeValues.iconUrl === undefined" @click="goToHome">Icone missing (upload it in Site theme)</div>
    <div v-else class="logo" :style="{backgroundImage:`url(${themeValues.iconUrl})`}" @click="goToHome">
    </div>
    <SearchBar ref="searchbaritem" class="search-bar" v-show="searchbarVisible" v-bind:themeValues="themeValues"/>
    <nav class="header-buttons">
      
      <i id="previous-button" class="material-icons header-buttons__button" @click="arrowNavigationClicked(-1)"
         v-show="navigationArrowVisible">skip_previous</i>
      <i id="next-button" class="material-icons header-buttons__button" @click="arrowNavigationClicked(1)"
         v-show="navigationArrowVisible">skip_next</i>
      <div v-if="isTagDefined">
        <i id="tag-selector" :class="tagClass" @click="tagSelectorClicked">bookmark</i>
        <TagSelector v-if="isModalOpen"/>
      </div>
      
      <!-- <button id="language-switch" class="button-nostyle"> -->
        <i class="material-icons header-buttons__button" @click="toggleLang">language</i> <span>{{lang}}</span>
      <!-- </button> -->
      <a href="https://github.com/simplicitesoftware" target="_blank">
        <i id="github" class="material-icons header-buttons__button">code</i>
      </a>
    </nav>
  </header>
</template>

<script>
  import {mapGetters, mapState} from "vuex";
  import SearchBar from "./SearchBar";
  import TagSelector from "./TagSelector";

  export default {
    name: "Header",
    data: () => ({
      searchbarVisible: true,
      navigationArrowVisible: false,
      defaultLogoUrl: "../../../public/Logo_Simplicite_Noir.png"
    }),
    components: { SearchBar, TagSelector },
    computed: {
      ...mapState({
        lesson: state => state.lesson.lesson,
        isDrawerOpen: state => state.ui.isDrawerOpen, // remove ?
        isModalOpen: state => state.ui.isModalOpen,
        themeValues: state => state.ui.themeValues,
      }),
      ...mapGetters({
        getLessonFromPath: 'tree/getLessonFromPath',
        lang: 'ui/lang',
        isTagDefined: 'ui/isTagDefined',
        isSortedByTag: "ui/isSortedByTag",
      }),
      tagClass() {
        return {
          'material-icons header-buttons__button': !this.isSortedByTag,
          'material-icons header-buttons__button tag-sorted': this.isSortedByTag,
        }
      }
    },
    methods: {
      goToHome() {
        this.$router.push('/').catch(() => console.log('Navigation Duplicated'))
      },
      arrowNavigationClicked(direction){
        let path = '';
        if (direction === -1) path = this.getLessonFromPath(this.lesson.path).previous_path;
        if (direction === 1) path = this.getLessonFromPath(this.lesson.path).next_path;
        if (path)
          this.$router.push('/lesson/' + path.toString().substring(1)).catch(err => console.error(err));
        else if (direction === -1) this.shakeElement("previous-button");
        else if (direction === 1) this.shakeElement("next-button");
      },
      toggleMenu() {
        this.$store.dispatch('ui/toggleDrawer');
      },
      toggleLang(){
        this.$store.dispatch('ui/toggleLang', {smp: this.$smp});
      },
      shakeElement(elementId) {
        document.getElementById(elementId).classList.add("shaked");
        setTimeout(() => document.getElementById(elementId).classList.remove('shaked'), 150);
      },
      tagSelectorClicked() {
        this.$store.commit('ui/TOGGLE_MODAL_STATE');
      }
    },
    created() {
      if (this.$router.currentRoute.name === 'Lesson') this.navigationArrowVisible = true
    },
    watch:{
      $route (to){
        this.navigationArrowVisible = to.name === 'Lesson';
      }
    },
  }
</script>

<style scoped lang="sass">
@import "../../assets/sass/variables"
@import "../../assets/sass/mixins"

header
  box-sizing: border-box
  width: 100%
  display: flex
  flex-flow: row
  align-items: center
  padding: $header-padding
  max-height: $header-height
  color: white
  .logo
    background-repeat: no-repeat
    background-size: contain
    z-index: 200
    width: $header-logo-width
    height: 50px
    margin: 5px 5px 5px 16px
    filter: invert(100%)
    &:hover
      cursor: pointer

  .logo-warning
    width: $header-logo-width
    height: $header-logo-height
    margin-top: map-get($margins, small)
    text-align: center
    &:hover
      cursor: pointer


  .menu-icon
    padding: $header-menu-icon-padding
    border-radius: 50px
    cursor: pointer
    display: flex
    align-items: center
    user-select: none
    &:hover
      background-color: transparentize(white, 0.9)
    &__image
      font-size: $header-menu-icon-size

  .header-buttons
    margin-left: auto
    display: flex
    align-items: center
    &__button
      margin-left: map-get($margins, small)
      padding: map-get($paddings, medium)
      border-radius: map-get($radius, x-large)
      color: white
      &:hover
        background-color: rgba(255, 255, 255, 0.1)
        cursor: pointer
  .tag-sorted
    color: rgba(242, 238, 99, 1)

  .shaked
    animation: headshake 100ms cubic-bezier(.4, .1, .6, .9)
    animation-iteration-count: 2

  .search-bar
    width: 50%

  @keyframes headshake
    0%
      background-color: $color-accent
      border: solid $color-accent
    25%
      transform: translateX(10%)
    75%
      transform: translateX(-10%)
</style>
