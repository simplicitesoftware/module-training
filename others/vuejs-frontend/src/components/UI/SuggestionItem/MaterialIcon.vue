<template>
    <div class="result">
        <div v-if="material" class="icon material-icons" v-html="iconSrc"></div>
        <img v-else class="svg" :src="iconSrc" alt="type">
    </div>
</template>

<script>
export default {
    name: "elastic-suggestion",
    data: function () {
        return {
            material: true,
            iconSrc: "",
        };
    },
    props: {
        type: String,
    },
    created() {
        switch (this.type) {
            case "simplicite":
                this.material = false;
                this.iconSrc = require("@/assets/simplicite-logo.svg");
                break;
            case "lesson":
                this.material = true;
                this.iconSrc = "article";
                break;
            case "discourse":
                this.material = false;
                this.iconSrc = require("@/assets/discourse-logo.svg");
                break;
            default:
                this.material = true;
                this.iconSrc = "book";
        }
    },
};
</script>

<style lang="sass" scoped>
.result
    align-self: center
    margin: 1rem 1rem 1rem 0
    border-radius: 3px
    padding: 0.5rem 0.5rem 0.3rem 0.5rem
    background: linear-gradient(to bottom, #F2F2F2 18%, #ebebeb)
    
    .icon,.svg
        width: 25px
        
    
</style>
