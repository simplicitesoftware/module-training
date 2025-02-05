<template>
    <div class="searchElement">
        <div class="searchbar-logo-container">
            <span class="material-icons searchbar-logo">
                search
            </span>
        </div>
        <input v-model="search" type="text" class="searchbar"/>
    </div>
</template>

<script>
export default {
    props: {
        modelValue: String
    },
    computed: {
        search: {
            get() { return this.modelValue; },
            set(value) {
                this.$emit('update:modelValue', value); // Emits the updated value
            }
        }
    }
}
</script>

<style scoped lang="sass">
@import "../../assets/sass/variables"
@import "../../assets/sass/mixins"

.searchElement
    display: flex
    align-items: center
    position: relative
    width: 100%

.searchbar
    padding-left: 2.5rem  // Space for the magnifying glass
    padding-right: 10px
    font-size: 1rem
    width: 100%
    height: 2.5rem  // Adjust the height for consistency
    border: 1px solid #ccc
    border-radius: 4px

.searchbar-logo-container
    position: absolute
    left: 10px
    display: flex
    justify-content: center
    align-items: center
    height: 100%
    width: 2.5rem  // Match the input height
    pointer-events: none  // Disable interaction with the icon
    .searchbar-logo
        font-size: 1.2rem  // Adjust size of the icon
        color: #555  // Adjust icon color for contrast

.searchbar:hover
    transition: border-color 0.3s ease

.searchbar:focus
    outline: none
    transition: border-color 0.3s ease
</style>
