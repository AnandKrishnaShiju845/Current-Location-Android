import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.R
import com.google.android.libraries.places.api.model.Place

class PlacesAdapter() : RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    private var placesList: List<Place> = emptyList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placesList[position]
        holder.bind(place)
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    fun updatePlacesList(newList: List<Place>) {
        placesList = newList
        notifyDataSetChanged()
    }

    class PlaceViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView){

        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
        private lateinit var currentPlace: Place



        fun bind(place: Place) {
            nameTextView.text = place.name
            addressTextView.text = place.address
            currentPlace = place
        }


    }



}


