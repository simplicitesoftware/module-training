/*
The mutations are all listed here so future developers can have a quick look of the mutations that can occur in the store
cf https://vuex.vuejs.org/guide/mutations.html#using-constants-for-mutation-types
*/
const SET_LESSON = 'SET_LESSON'
const SET_LESSON_IMAGES = 'SET_LESSON_IMAGES'
const SET_LESSON_TAGS = 'SET_LESSON_TAGS';
const UNSET_LESSON = 'UNSET_LESSON'
const SET_TREE = 'SET_TREE'
const OPEN_NODE = 'OPEN_NODE'
const TOGGLE_NODE_OPEN = 'TOGGLE_NODE_OPEN'
const SET_STYLE = 'SET_STYLE';
const SET_DRAWER_STATE = 'SET_DRAWER_STATE'
const SET_LIGHT_BOX_IMAGE = 'SET_LIGHT_BOX_IMAGE'
const SET_LIGHT_BOX_VISIBILITY = 'SET_LIGHT_BOX_VISIBILITY'
const TOGGLE_LANG = 'TOGGLE_LANG';
const TOGGLE_MODAL_STATE = 'TOGGLE_MODAL_STATE';
const SET_TAG_LIST = 'SET_TAG_LIST';
const TOGGLE_TAG_UI_SELECTION = 'TOGGLE_TAG_UI_SELECTION';
const SET_TAG_LIST_SELECTION = 'SET_TAG_LIST_SELECTION';
const DEFAULT_TAG_LIST = 'DEFAULT_TAG_LIST';
const TAG_MODAL_CANCELLATION = 'TAG_MODAL_CANCELLATION';
const SET_LESSON_HTML = 'SET_LESSON_HTML';


export {
  SET_LESSON, SET_LESSON_HTML, SET_LESSON_IMAGES, SET_LESSON_TAGS, UNSET_LESSON,
  SET_TREE,OPEN_NODE,TOGGLE_NODE_OPEN, SET_STYLE,
  SET_DRAWER_STATE, SET_LIGHT_BOX_IMAGE, SET_LIGHT_BOX_VISIBILITY, 
  SET_TAG_LIST, TOGGLE_TAG_UI_SELECTION, SET_TAG_LIST_SELECTION, DEFAULT_TAG_LIST, TAG_MODAL_CANCELLATION,
  TOGGLE_LANG,
  TOGGLE_MODAL_STATE,
}

