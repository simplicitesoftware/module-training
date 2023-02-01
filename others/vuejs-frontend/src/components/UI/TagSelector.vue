<template>
  <transition name="modal-fade">
    <div class="modal" role="dialog">
      <div class="modal-dialog" v-click-outside="onClickOutside">
        <div class="modal-content">
          <div class="modal-header">
            <h4 class="modal-header__title">{{ titleTranslation }}</h4>
          </div>
          <div>
            <div class="modal-description">{{ componentDescription }}</div>
          </div>
          <div class="modal-body">
            <button
              v-for="(item, index) in tagList"
              :key="index"
              class="card"
              v-bind:class="{ active: item.uiSelected }"
              v-on:click="selectTag(index)"
            >
              {{ item.display_value }}
            </button>
          </div>
          <div class="modal-footer">
            <button class="confirm" v-on:click="confirmChoice">
              {{ confirmButtonTranslation }}
            </button>
            <button class="show-all" v-on:click="showAll">
              {{ showAllButtonTranslation }}
            </button>
            <button class="cancel" v-on:click="cancelUiSelection">
              {{ cancelButtonTranslation }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import { mapGetters, mapState } from "vuex";

export default {
  name: "TagSelector",
  computed: {
    ...mapState({
      tagList: (state) => state.ui.tagList,
    }),
    ...mapGetters({
      lang: "ui/lang",
      getLessonFromPath: 'tree/getLessonFromPath'
    }),
    // replace with server translation ?
    titleTranslation() {
      return "FRA" === this.lang ? "Filtrer les leçons" : "Filter lessons";
    },
    componentDescription() {
      return "FRA" === this.lang
        ? "Cliquez sur les thématiques que vous souhaitez afficher"
        : "Pick the themes you want to display";
    },
    confirmButtonTranslation() {
      return "FRA" === this.lang ? "Confirmer" : "Confirm";
    },
    cancelButtonTranslation() {
      return "FRA" === this.lang ? "Annuler" : "Cancel";
    },
    showAllButtonTranslation() {
      return "FRA" === this.lang ? "Tout voir" : "Show all";
    },
  },
  methods: {
    // close modal when the modal-placeholder div has been clicked || cancel button
    onClickOutside() {
      this.cancelUiSelection();
    },
    // change data
    selectTag(index) {
      this.$store.commit("ui/TOGGLE_TAG_UI_SELECTION", index);
    },
    confirmChoice() {
      this.$store.commit("ui/SET_TAG_LIST_SELECTION");
      this.$store.commit("ui/TOGGLE_MODAL_STATE");
      this.$store.dispatch("tree/fetchTree", { smp: this.$smp })
    },
    showAll() {
      this.$store.commit("ui/DEFAULT_TAG_LIST");
      this.$store.commit("ui/TOGGLE_MODAL_STATE");
      this.$store.dispatch("tree/fetchTree", { smp: this.$smp })
      .then(() => this.$store.commit("tree/OPEN_NODE", "/"+this.$router.currentRoute.params.lessonPath));
    },
    cancelUiSelection() {
      this.$store.commit("ui/TAG_MODAL_CANCELLATION");
      this.$store.commit("ui/TOGGLE_MODAL_STATE");
    },
  },
};
</script>

<style scoped lang="sass">
.modal-fade-enter

.modal
  position: fixed
  z-index: 2000
  display: block
  overflow-x: hidden
  overflow-y: auto
  top: 0
  left: 0
  width: 100%
  height: 100%
  outline: 0
  background-color: rgba(0,0,0,0.35)
  color: black
  text-align: center
  

.modal-dialog
  position: relative
  width: auto
  border: 10px
  max-width: 500px
  margin: 1.75rem auto
  

.modal-header
  margin: auto
  width: auto
  padding: 10px
  font-size: 1.8rem
  border-radius: 0.25rem
  border-bottom: 1px solid #eee

.modal-description
  margin-top: 2%
  font-size: 1.2rem

.modal-content
  text-align: center
  color: black
  background-color: white
  border-width: 1px
  border-radius: 15px

.modal-body
  display: flex
  flex-direction: row
  flex-wrap: wrap
  justify-content: space-around
  height: auto
  overflow-y: auto

.card
  border: solid #e0e0e0
  border-width: 2px
  border-radius: 5px
  padding: 2%
  margin: 4%
  background: #fcfcfc
  font-size: 1.2rem
  &:hover
    box-shadow: 0 .3125rem 1.875rem rgba(43,135,218,.2)
    cursor: pointer
  &.active
    background: #394b54
    color: white

.modal-footer
  display: flex
  justify-content: space-evenly
  margin: .25rem
  padding-bottom: 2rem
  padding-top: 1rem

.confirm, .cancel, .show-all
  border-radius: .2em
  color: #FFFFFF
  text-decoration: none
  font-size: 1.2rem
  padding: .5em .8em
  cursor: pointer

.confirm
  background-color: #4CAF50
  border-color: #4CAF50  
  &:hover
    box-shadow: 0 .3125rem 1.875rem rgba(43,135,218,.2)

.cancel
  background-color: #F00
  border-color: #F00  
  &:hover
    box-shadow: 0 .3125rem 1.875rem rgba(43,135,218,.2)

.show-all
  background-color: #555555
  border-color: #555555
  &:hover
    box-shadow: 0 .3125rem 1.875rem rgba(43,135,218,.2)
</style>