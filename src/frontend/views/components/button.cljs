(ns frontend.views.components.button)

(defn component
  ([txt]
   [component {} txt])
  ([options txt]
   [:button (merge {:type "button"
                    :class "py-2 px-4 bg-green-500 text-white font-semibold rounded-lg shadow-md hover:bg-green-700 focus:outline-none disabled:opacity-50 disabled:bg-green-500"}
                   options)
    txt]))
