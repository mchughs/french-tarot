(ns frontend.views.pages.about)

(defn page []
  [:div.py-5
   [:div.lg:flex.bg-gray-100.p-8.lg:p-0
    [:img.lg:rounded-xl.rounded-full.mx-auto
     {:alt ""
      :src "http://localhost:5444/assets/images/profile_picture.jpeg"}]
    [:div.pt-6.lg:p-8.text-center.lg:text-left.space-y-4
     [:h2.text-2xl "About the programmer:"]
     [:p "Hi, my name is Samuel McHugh. I'm a programmer from the United States 🇺🇸 and France 🇫🇷.
        I've worked on a variety of consumer-facing, Clojure-based webapps.
        If you have a project which uses Clojure, you may benefit from my years of production experience.
        Feel free to checkout my links below and get in touch."]]]
   
   [:div.p-6.flex.flex-wrap.justify-around
    [:a.flex.flex-col.place-items-center.p-2.justify-end
     {:target "_blank" 
      :href "https://www.linkedin.com/in/samuel-mchugh/"}
     [:img {:class "inline-block w-20 rounded-md"
            :src "http://localhost:5444/assets/images/linkedin.png"
            :alt ""}]
     "My LinkedIn"]
    [:a.flex.flex-col.place-items-center.p-2.justify-end
     {:target "_blank" 
      :href "https://github.com/mchughs"}
     [:img {:class "inline-block w-16 rounded-md"
            :src "http://localhost:5444/assets/images/github.png"
            :alt ""}]
     "My GitHub"]
    [:a.flex.flex-col.place-items-center.p-2.justify-end
     {:href "mailto:smchugh230395@gmail.com?subject=Clojure-Opportunity"}
     [:img {:class "inline-block w-16 rounded-md"
            :src "http://localhost:5444/assets/images/gmail.png"
            :alt ""}]
     "My Email"]
    [:form.flex.flex-col.p-2.justify-end
     {:method "get"
      :action "http://localhost:5444/assets/Samuel_McHugh_Resume.pdf"}
     [:button.basic.flex.flex-col.place-items-center
      {:type "submit"}
      [:img {:class "inline-block w-16 rounded-md"
             :src "http://localhost:5444/assets/images/file-download.svg"
             :alt ""}]
      "My Résumé"]]]
   
   [:p "I grew up playing French Tarot with my family and I wanted to try my hand at bringing this fun game to the internet.
        Maybe the game will get a second wind with a newer generation of players."]])

