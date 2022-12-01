<template>
  <div class="tree">
    <p :style="indent" @click="nodeClicked(node)" class="tree__root-label"
       :class="[lesson.path && lesson.path === node.path ? 'active' : '']">
      <i v-if="(node.categories && node.categories.length) || (node.lessons && node.lessons.length)"
         class="material-icons tree__arrow" :class="[node.open ? 'down-arrow' : '']" >keyboard_arrow_right</i>
      <i v-if="node.is_category" class="material-icons tree__node-type">folder</i>
      <i v-else class="material-icons tree__node-type">menu_book</i>
      <span>{{node.title}}</span>
    </p>

    <div v-show="node.open" class="tree__subtree">
      <TreeViewNode v-for="(subCategory) in node.categories" v-bind:key="subCategory.path" :node="subCategory" :depth="depth+1"/>
      <TreeViewNode v-for="(lesson) in node.lessons" v-bind:key="lesson.path" :node="lesson" :depth="depth+1"/>
    </div>
  </div>

</template>

<script>
  import {mapState} from 'vuex';

  export default {
    props: {
      node: {
        type: Object,
        required: true,
        default: () => ({}),
        note: 'The node object containing the node\'s information and children nodes',
      },
      depth: {
        type: Number,
        required: true,
        default: 0,
        note: 'The depth of the node, to create the space before the node',
      },
    },
    name: "TreeViewNode",
    computed: {
      ...mapState({
        lesson: state => state.lesson.lesson
      }),
      indent() {
        if (this.depth === 0) return {'padding-left': `10px`}; // Root elements
        else if (this.node.title ) return {'padding-left': `${(this.depth + 1) * 20}px`}; // Lessons elements. +5 is here because of the margin on the span element. This way it is more clean
        else return {'padding-left': `${this.depth * 20}px`}
      }
    },
    methods: {
      nodeClicked(node) {
        if (node.is_category) this.$store.commit('tree/TOGGLE_NODE_OPEN', node.path);
        else this.$router.push('/lesson' + node.path).catch(err => console.error(err))
      },
    }
  }
</script>

<style lang="sass" scoped>
  @import "../../assets/sass/variables"
  @import "../../assets/sass/mixins"

  .tree
    //Really important for global rendering. Otherwise, when the side menu is collapsed, the height of the menu
    //gets bigger than the page because the words in the treeview wrap
    // overflow: hidden
    white-space: nowrap
    margin: $tree-margin
    user-select: none
    color: white

  .tree__root-label
    width: 100%
    font-size: map-get($title-sizes, 6)
    font-weight: lighter
    display: flex
    align-items: center
    padding: $tree-padding-node
    margin: 0
    @include rounded-right-corners($tree-border-radius, $tree-border-radius)
    &:hover
      background-color: $color-tree-hover
      cursor: pointer
    &.active
      background-color: $color-tree-hover
    span
      margin-left: map-get($margins, x-small)
    .tree__arrow
      transition: $tree-duration-arrow-rotation all
      font-size: $tree-arrow-size
      &.down-arrow
        transform: rotate(90deg)
</style>