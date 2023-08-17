<template>
    <div v-if="material" class="result-icon material-icons" v-html="iconSrc"></div>
    <img v-else class="result-icon" :src="iconSrc" alt="type">
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
        console.log(this.type);
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
.result-icon
    align-self: center
    margin: 1rem
    border-radius: 3px
    padding: 0.5rem
    background: linear-gradient(to bottom, #F2F2F2 18%, #ebebeb)
    width: 48px
    height: auto
</style>
