import {SET_LESSON, SET_LESSON_IMAGES, UNSET_LESSON, SET_LESSON_TAGS} from "../mutation-types";

export default {
  namespaced: true,
  state: {
    lesson: {},
    lessonImages: [],
    lessonTags: [],
  },
  actions: {
    async openLesson({dispatch, commit} , payload) {
      commit('tree/OPEN_NODE', payload.lesson.path, { root: true });
      await dispatch("fetchLessonTag", payload);
      await dispatch("fetchLessonContent", payload);
      await dispatch("fetchLessonImages", payload);
    },

    async fetchLessonContent({commit, rootGetters}, payload) {
      return new Promise((resolve) => {
        payload.smp.getExternalObject('TrnTreeService').call(
          {
            lang:rootGetters['ui/lang'],
            getLesson:payload.lesson.row_id
          }
        ).then(function(res){
          commit(SET_LESSON, res);
          resolve();
        })
      })
    },

    async fetchLessonImages({commit}, payload) {  
      return new Promise((resolve, reject) => {
        let picture = payload.smp.getBusinessObject("TrnPicture");
        picture.search(
          {'trnPicLsnId': payload.lesson.row_id}, {inlineDocs: 'infos'}
        ).then(array => {
          if(array){
            commit(SET_LESSON_IMAGES, array.map(pic => ({
              filename: pic.trnPicImage.name,
              filesize: pic.trnPicImage.size,
              filesrc: picture.getFieldDocumentURL("trnPicImage", pic)
            })))
            resolve();
          }
          else
            reject("Impossible to fetch the pictures");
        })
      });
    },

    async fetchLessonTag({commit, rootGetters}, payload) {
      return new Promise((resolve, reject) => {
        let lessonTags = payload.smp.getBusinessObject("TrnTagLsn");
        lessonTags.search(
          {'trnTaglsnLsnId': payload.lesson.row_id}
        ).then(array => {
          if(array) {
            commit(SET_LESSON_TAGS, array.map(tag => rootGetters['ui/getTagDisplayWithRowId'](tag.trnTaglsnTagId)));
            resolve();
          }
          else
            reject("Impossible to fetch the lesson's tag(s)");
        })
      })
    },

    unsetLesson({commit}) {
      commit(UNSET_LESSON);
    }
  },
  mutations: {
    [SET_LESSON](state, lesson) {
      state.lesson = lesson;
    },
    [SET_LESSON_IMAGES](state, images) {
      state.lessonImages = images;
    },
    [UNSET_LESSON](state) {
      state.lesson = {};
      state.lessonImages = [];
    },
    [SET_LESSON_TAGS](state, lessonTags) {
      state.lessonTags = lessonTags;
    }
  }
}