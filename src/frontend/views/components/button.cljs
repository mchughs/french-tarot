(ns frontend.views.components.button)

(def themes
  {:base "py-2 px-4 bg-green-500 text-white font-semibold rounded-lg shadow-md hover:bg-green-700 focus:outline-none disabled:opacity-50 disabled:bg-green-500 w-24"
   :alt "py-2 px-4 bg-red-500 text-white font-semibold rounded-lg shadow-md hover:bg-red-700 focus:outline-none disabled:opacity-50 disabled:bg-red-500 w-24"})

(defn component
  ([txt]
   [component {} txt])
  ([{:keys [theme] :as options} txt]
   (let [theme (or theme :base)]
     [:button (merge {:type "button" :class (get themes theme)}
                     options)
      txt])))
