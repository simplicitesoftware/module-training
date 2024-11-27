/* eslint-disable no-debugger */
import { defineStore } from 'pinia';
import { useTreeStore } from './tree';
import { useUiStore } from './ui';
export const useLessonStore = defineStore('lessonStore', {
    namespaced: true,
    state: () => ({
        lesson: {},
        lessonImages: [],
        lessonTags: [],
        
    }),
    actions: {
        async openLesson(payload) {
          const treeStore = useTreeStore();
            treeStore.OPEN_NODE(payload.lesson.path, { root: true });
            await this.fetchLesson(payload);
        },
    async openHomePage(payload) {
      return new Promise((resolve, reject) => {
        let page = payload.smp.getBusinessObject("TrnPage");
        page.search(
          {"trnPageType": "homepage", "trnPageTrnLessonid__trnLsnPublish": true}, {inlineDocs: 'infos'}
        ).then(async array => {
          if(array[0]) {
            payload.lesson.row_id = array[0].trnPageTrnLessonid;
            payload.lesson.viz = array[0].trnPageTrnLessonid__trnLsnVisualization;
            await this.fetchLesson(payload);
            resolve();
          }
          reject('Unable to fetch homepage from backend');
        });
      });  
    },

    async openPage(payload) {
      return new Promise((resolve, reject) => {
        let page = payload.smp.getBusinessObject("TrnPage");
        page.search({"trnPageTrnLessonid__trnLsnFrontPath": payload.path, "trnPageTrnLessonid__trnLsnPublish": true}, {inlineDocs: 'infos'})
        .then(async array => {
          if(array[0]) {
            payload.lesson.row_id = array[0].trnPageTrnLessonid;
            payload.lesson.viz = array[0].trnPageTrnLessonid__trnLsnVisualization;
            await this.fetchLesson(payload);
            resolve();
          } else {
            reject();
          }
        }).catch((e) => {
            console.log(e);
            reject();
        })
      })
    },

    async fetchLesson(payload) {
      const uiStore = useUiStore();
      await this.fetchLessonTag(payload);
      await this.fetchLessonContent(payload,uiStore.lang);
      // only fetch image on other than Linear mode
      // See on Training module in TrnLesson.java -> setLinearPictureContent()
      if(payload.lesson.viz !== 'LINEAR') {
        await this.fetchLessonImages(payload,uiStore.lang);
      }
    },

    async fetchLessonContent(payload,lang) {
      const lesonStore = this;
      return new Promise((resolve) => {
        payload.smp.getExternalObject('TrnTreeService').call(
          {
            lang:lang,
            getLesson:payload.lesson.row_id
          }
        ).then(function(res){
          lesonStore.SET_LESSON(res);
          resolve();
        })
      })
    },

    async fetchLessonImages(payload,lang) {  
      return new Promise((resolve, reject) => {
        let picture = payload.smp.getBusinessObject("TrnPicture");
        picture.search(
          {'trnPicLsnId': payload.lesson.row_id,'trnPicLang': `in ('ANY', '${lang}')`}, {inlineDocs: 'infos'}
        ).then(array => {
          if(array){
            this.SET_LESSON_IMAGES(array.map(pic => ({
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

    async fetchLessonTag(payload) {
      return new Promise((resolve, reject) => {
        const uiStore = useUiStore();
        let lessonTags = payload.smp.getBusinessObject("TrnTagLsn");
        lessonTags.search(
          {'trnTaglsnLsnId': payload.lesson.row_id}
        ).then(array => {
          if(array) {
            this.SET_LESSON_TAGS(array.map(tag =>uiStore.getTagDisplayWithRowId(tag.trnTaglsnTagId)));
            resolve();
          }
          else
            reject("Impossible to fetch the lesson's tag(s)");
        })
      })
    },
   
    unsetLesson() {
      this.lesson = {};
      this.lessonImages = [];
    },
    SET_LESSON(lesson){
      this.lesson = lesson;
    },
    SET_LESSON_HTML(html){
      this.lesson.html = html;
    },
    SET_LESSON_IMAGES(images){
      this.lessonImages = images;
    },
    SET_LESSON_TAGS(tags){
      this.lessonTags = tags;
    }
  }
});
