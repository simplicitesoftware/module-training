import VueRouter from 'vue-router'
import Home from "./components/Pages/Home"
import Lesson from "./components/Pages/Lesson"
import PageNotFound from "./components/Pages/PageNotFound"
import SandBoxDeployment from "./components/Pages/SandBox/Deployment"
import SandBoxDemand from "./components/Pages/SandBox/Demand"
import TagNotContent from "./components/UI/TagNoContent"
import AdvancedSearch from "./components/Pages/AdvancedSearch.vue"

//1. Routes definition
const routes = [
    { path: '/', component: Home, name: 'Home' },
    { path: '/lesson/:lessonPath(.*)', component: Lesson, name: 'Lesson' },
    { path: '/page/:pagePath(.*)', component: Lesson, name: 'Page' },
    { path: '/category/:categoryPath(.*)', component: Lesson, name: 'Category' },
    { path: '/search/:query(.*)', component: AdvancedSearch, name: 'AdvancedSearch' },
    { path: '/sandbox/:demandId', component: SandBoxDeployment, name:'SandBoxDeployment' },
    { path: '/sandbox-demand', component: SandBoxDemand, name: 'SandBoxDemand' },
    { path: '/tag-no-content', component: TagNotContent, name: 'TagNoContent'},
    { path: '/*', component: PageNotFound, name: 'PageNotFound' }
];

//2. Exporting the router instance
export default  new VueRouter({
    mode: 'history',
    routes
});