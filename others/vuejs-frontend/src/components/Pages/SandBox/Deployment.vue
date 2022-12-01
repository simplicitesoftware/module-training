<template>
  <div class="wrapper">
    <div class="sandbox-redirect">
      <h2 class="sandbox-redirect__title">On se met au boulot, votre sandbox arrive bientôt !</h2>
      <img class="sandbox-image" src="https://megastuces.com/wp-content/uploads/2017/02/sandbox.png"
           alt="sandbox image">
      <h2 class="sandbox-redirect__title">Quelques infos sur votre sandbox ...</h2>
      <p>Pour démarrer au mieux avec votre Simplicité, voici les liens vers quelques ressources utiles :</p>
      <ul class="sandbox-redirect__list">
        <li>
          <p>Notre site de formation (gratuit) : <a target="_blank" href="https://docs2.dev.simplicite.io">https://docs2.dev.simplicite.io</a>
          </p>
          <p>Vous y trouverez toutes les ressources pour apprendre à vous servir de Simplicité. Pour votre
            première prise en main, nous vous conseillons de suivre la formation, pour vous apprendre les bases
            du développement sur notre Plateforme.</p>
        </li>
        <li>
          <p>Notre forum communautaire : <a target="_blank" href="http://community.simplicite.io/">http://community.simplicite.io/</a>
          </p>
          <p>Nos utilisateurs posent leurs questions et s'entraident sur ce forum. Vous y trouverez également des tips &
            tricks utiles.</p>
        </li>
        <li>
          <p>Notre documentation : <a target="_blank" href="https://docs.simplicite.io/">https://docs.simplicite.io/</a>
          </p>
          <p>Ici, vous trouverez la documentation de Simplicité.</p>
        </li>

      </ul>
      <h4 class="end-text">Voilà, c'est tout ! Si vous êtes arrivés jusqu'ici, allez vérifier vos mails, vous devriez
        avoir une bonne surprise. A tout de suite sur Simplicité !</h4>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'Deployment',

    mounted() {
      let demandId = this.$router.currentRoute.path.split("/sandbox/")[1];
      let baseURL = "https://portalpr.dev.simplicite.io/ext/PorIsdService?identifier=";
      let req = new XMLHttpRequest();
      req.open("GET", baseURL + demandId, true);
      req.send(null);
      console.log(req.responseText);
    }
  }

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>

  @import "../../../assets/sass/mixins";
  @import "../../../assets/sass/variables";

  .wrapper {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  a {
    color: $color-primary
  }

  .sandbox-redirect {
    width: 70%;
    padding: map-get($paddings, large);
    display: flex;
    flex-flow: column nowrap;
    border-radius: map-get($radius, regular);;
    background-color: $color-form-background;
    @include box-shadow;

    &__title {
      font-weight: bold;
      color: $color-form-title;
      font-size: map-get($title-sizes, 2);
      margin-bottom: map-get($margins, medium);
    }

    .end-text {
      font-size: 1.3rem;
    }

    &__list {
      padding: 0;
      margin-top: 20px;

      li {
        margin-bottom: 20px;

        p {
          font-size: 1.1rem;
        }
      }

    }

  }

  .sandbox-image {
    max-width: $redirect-image-width;
    align-self: center;
    margin-bottom: map-get($margins, medium);
    border-radius: map-get($radius, regular);;
  }

</style>
