<template>

  <div class="wrapper">
    <div class="sandbox-demand">
      <div class="sandbox-form">
        <h1 class="sandbox-form__title">Demandez une Sandbox</h1>
        <form>
          <p class="sandbox-form__element">
            <input id="email" class="sandbox-form__input required" type="email" name="email" placeholder="Votre email"
                   required/>
          </p>
          <p class="sandbox-form__element">
            <input id="name" class="sandbox-form__input" type="text" name="name" placeholder="Nom"/>
          </p>
          <p class="sandbox-form__element">
            <input id="firstName" class="sandbox-form__input" type="text" name="firstName" placeholder="Prénom"/>
          </p>
          <p class="sandbox-form__element">
            <input id="company" class="sandbox-form__input" type="text" name="company" placeholder="Société"/>
          </p>
          <p class="sandbox-form__element">
            <input id="phone" class="sandbox-form__input" type="tel" name="phone" placeholder="Téléphone"/>
          </p>
          <div class="sandbox-form__element">
            <label for="profile">Profil</label>
            <select id="profile" name="profile">
              <option value="NA" selected>--</option>
              <option value="Profil technique (IT)">Profil technique (IT)</option>
              <option value="Profil métier">Profil métier</option>
              <option value="Étudiant">Etudiant</option>
              <option value="Autre">Autres</option>
            </select>
          </div>
          <p>
            <input id="newsletter" class="newsletter-input" type="checkbox" name="newsletter"/>
            <label for="newsletter">Vous acceptez la newsletter</label>
          </p>
        </form>
        <button v-show="!showSpinner" class="validate-button" @click="sendDemand()">Essayer Gratuitement</button>
        <div v-if="showSpinner" class="spinner-wrapper">
          <Spinner/>
        </div>
        <div v-show="serverDown" class="server-error">Il semble qu'il y ait eu une erreur. Veuillez réessayer</div>

      </div>
      <div class="vertical-separator"></div>
      <div class="side-content">
        <div class="side-content__brand">
          <h2 class="side-content__message">Créez vos applications dès maintenant avec Simplicité</h2>
          <ul>
            <li><img class="side-content__icon" src="../../../../public/hand-pen.png">Remplissez le formulaire</li>
            <li><img class="side-content__icon" src="../../../../public/email.png">Validez votre demande avec l'email
            </li>
            <li><img class="side-content__icon" src="../../../../public/rocket.png">C'est tout bon, à vous de jouer !
            </li>
          </ul>
          <img class="side-content__brand-image" src="../../../../public/developer.png" alt="computer image">
        </div>

      </div>
    </div>
  </div>


</template>

<script>
  import Spinner from "../../UI/Spinner";

  export default {
    name: "Demand",
    components: {Spinner},
    data: () => ({
      showSpinner: false,
      serverDown: false,
      clickCounter: 0,
    }),
    methods: {
      sendDemand: function () {
        console.log("COUNTER ===" + this.clickCounter);
        if (this.clickCounter === 0) {
          let email = document.getElementById("email");
          if (email.value !== "" && email.value !== undefined && email.value !== null) {
            this.clickCounter++;
            this.serverDown = false;
            document.getElementsByClassName("validate-button")[0].innerText = "Demande Envoyée";
            this.showSpinner = true;
            setTimeout(() => {
              let json = this.generateJSON();
              let req = this.generateRequest();
              req.send(json);
            }, 1500)
          } else if (!email.classList.contains("empty-input")) {
            email.classList.add("empty-input")
          }
        }
      },
      generateJSON: function () {
        let email = document.getElementById("email");
        let name = document.getElementById("name").value;
        let firstName = document.getElementById("firstName").value;
        let company = document.getElementById("company").value;
        let phone = document.getElementById("phone").value;
        let profile = document.getElementById("profile").value;
        let newsletter = document.getElementById("newsletter").value;
        return JSON.stringify({
          "email": email.value, "name": name, "firstName": firstName, "company": company, "phone": phone,
          "profile": profile, "newsletter": newsletter
        });
      },
      generateRequest: function () {
        let btn = document.getElementsByClassName("validate-button")[0];
        let req = new XMLHttpRequest();
        req.open("POST", "https://portalpr.dev.simplicite.io/ext/PorIsdService", true);
        req.setRequestHeader("Content-type", "application/json");
        req.addEventListener("load", (result) => {
          console.log(result)
          if (req.status >= 200 && req.status < 400) {
            console.log(req)
            this.showSpinner = false;
            btn.classList.add("server-ok");
            btn.innerText = "Un email vous a été envoyé !"
          } else {
            console.error(req.status + " " + req.statusText);
            this.clickCounter = 0;
            this.showSpinner = false;
            this.serverDown = true;
            if (req.status === 401) {
              document.getElementsByClassName("server-error")[0].innerText = "Cette adresse mail est invalide";
            }
            btn.innerText = 'Renvoyer une demande'
          }
        });
        req.addEventListener("error", () => {
          this.showSpinner = false;
          this.serverDown = true;
          console.error("Erreur réseau avec l'URL ")
        });
        return req;
      },
    },
    mounted() {
      let mail = document.getElementById("email")
      mail.addEventListener("blur", () => {
        if (mail.value === "" || mail.value === undefined || mail.value === null) {
          mail.placeholder = "Veuillez renseigner votre email";
          mail.classList.add("empty-input")
        } else
          mail.classList.remove("empty-input")
      })
      mail.addEventListener("focus", () => {
        mail.classList.remove("empty-input")
        mail.placeholder = "";
      })
    }
  }
</script>

<style lang="scss" scoped>
  @import "../../../assets/sass/variables";
  @import "../../../assets/sass/mixins";

  .wrapper {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: $color-form-background;
  }

  .sandbox-demand {
    width: $width-sandbox-form;
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    border-radius: map-get($radius, regular);;
    @include box-shadow;
  }

  .sandbox-form {
    width: $width-sandbox-form;
    padding: map-get($paddings, large);
    display: flex;
    flex-flow: column;

    &__title {
      font-weight: bold;
      color: $color-form-title;
      font-size: map-get($title-sizes, 2);
      margin-bottom: map-get($margins, large);
    }

    label {
      margin: 0;
    }

    &__element {
      display: flex;
      flex-flow: column;
      margin-bottom: map-get($margins, medium);
    }


    &__input {
      border: none;
      border-bottom: 1px solid $color-border-form-element;

      &::placeholder {
        color: $color-border-form-element;
      }

      &:focus {
        border-bottom: solid 1px $color-focus-form-element;

        &::placeholder {
          color: $color-focus-form-element;
        }
      }
    }

    .newsletter-input {
      margin-right: 10px;
    }

    .empty-input {
      border-bottom: 1px solid $color-invalid-email;

      &::placeholder {
        color: $color-invalid-email;
      }
    }

    .validate-button {
      box-sizing: border-box;
      padding: map-get($paddings, large);
      border: none;
      border-radius: map-get($radius, regular);;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: map-get($title-sizes, 5);
      background-color: lighten($color-primary, 10%);
      color: $color-button-text;

      &.server-ok {
        background-color: $color-form-background;
        color: $color-accent;
        border: solid 2px $color-accent;

        &:hover {
          background-color: lighten($color-accent, 50%);
        }
      }
    }
  }

  .vertical-separator {
    width: 2px;
    background-color: $color-separator;
    height: $height-separator;
    align-self: center;
    margin: 0 map-get($margins, small);
  }

  .side-content {
    width: (100%-$width-sandbox-form);
    border-radius: 0 map-get($radius, regular) map-get($radius, regular) 0;
    padding: 20px;
    position: relative;
    display: flex;
    flex-flow: column nowrap;
    justify-content: space-around;
    color: $color-form-title;

    &__brand {
      display: flex;
      flex-direction: column;

      ul {
        margin: map-get($margins, medium) 0;
      }
    }

    &__brand-image {
      max-width: 100%;
    }

    &__message {
      font-size: 2rem;
      font-weight: bold;
      margin-bottom: 30px;
    }

    &__icon {
      width: $width-sandbox-icon;
      height: $width-sandbox-icon;
      margin-right: 10px;
    }
  }

  .spinner-wrapper {
    padding: 100px;
  }

  .server-error {
    border-radius: map-get($radius, regular);;
    background-color: $color-error;
    padding: map-get($paddings, medium);
    margin-top: 20px;
  }
</style>
