import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.khushu.R
import com.example.khushu.lib.Place
import com.example.khushu.ui.home.HomeViewModel

class ItemAdapter(private val viewModel: HomeViewModel) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var items: List<Place> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item.name
        holder.removeButton.setOnClickListener {
            viewModel.removeItem(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Place>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun removeItemAt(position: Int) {
        items = items.toMutableList().also { it.removeAt(position) }
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val removeButton: Button = itemView.findViewById(R.id.removeButton)
    }
}