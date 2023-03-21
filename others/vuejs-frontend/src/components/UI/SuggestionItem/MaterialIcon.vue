<template>
     <div class="result-icon material-icons"  v-html="materialIconCode"></div>
</template>

<script>
export default {
    name: "elastic-suggestion",
    props:{
        cat: String,
    },
    computed: {
        materialIconCode: function(){
            var materialCode = '';
            switch(this.cat){
            case "Configuration":
                materialCode = "design_services"
                break;
            case "Development":
                materialCode = "code"
                break;
            case "Apis":
                materialCode = "api"
                break;
            default:
                materialCode = "book"
            }
            return materialCode
        },
    }
}
    
</script>

<style lang="sass" scoped>
.result-icon
    align-self: center
    margin: 1rem
    border-radius: 3px
    padding: .5rem
    background: linear-gradient(to bottom, #F2F2F2 18%, #E2E2E2)
</style>