package com.tutorials.eu.favdish.view.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutorials.eu.favdish.R
import com.tutorials.eu.favdish.application.FavDishApplication
import com.tutorials.eu.favdish.databinding.DialogCustomListBinding
import com.tutorials.eu.favdish.databinding.FragmentAllDishesBinding
import com.tutorials.eu.favdish.model.data.FavDish
import com.tutorials.eu.favdish.utils.Constants
import com.tutorials.eu.favdish.view.activities.MainActivity
import com.tutorials.eu.favdish.view.adapters.CustomListItemAdapter
import com.tutorials.eu.favdish.view.adapters.FavDishAdapter
import com.tutorials.eu.favdish.viewmodel.FavDishViewModel
import com.tutorials.eu.favdish.viewmodel.FavDishViewModelFactory

class AllDishesFragment : Fragment() {

    private lateinit var mbindig: FragmentAllDishesBinding

    // A global variable for Filter List Dialog
    private lateinit var mCustomListDialog: Dialog

    // A global variable for FavDishAdapter Class
    private lateinit var favDishAdapter: FavDishAdapter

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository, requireActivity().application)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mbindig.rvDishesList.layoutManager = GridLayoutManager(requireContext(), 2)
        favDishAdapter = FavDishAdapter(this@AllDishesFragment){
            val action = AllDishesFragmentDirections.actionNavigationAllDishesToAddUpdateDishActivity(it)
            this.findNavController().navigate(action)
        }
        mbindig.rvDishesList.adapter = favDishAdapter

        mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) { dishes ->
            dishes.let {
                if(it.isNotEmpty()){
                    mbindig.rvDishesList.visibility = View.VISIBLE
                    mbindig.tvNoDishesAddedYet.visibility = View.GONE

                    favDishAdapter.dishesList(it)
                } else {
                    mbindig.rvDishesList.visibility = View.GONE
                    mbindig.tvNoDishesAddedYet.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mbindig = FragmentAllDishesBinding.inflate(inflater, container, false)
        return mbindig.root
    }

    fun dishDetail(favDish: FavDish){
        findNavController().navigate(AllDishesFragmentDirections.actionNavigationDishDetail(favDish))
        if (requireActivity() is MainActivity){
            (activity as MainActivity?)!!.hideButtonNavigationView()
        }
    }

    /**
     * Method is used to show the Alert Dialog while deleting the dish details.
     *
     * @param dish - Dish details that we want to delete.
     */
    private fun deleteDish(dish: FavDish) {
        mFavDishViewModel.delete(dish)
    }

    fun showConfirmationDialog(dish: FavDish) {
        val builder = AlertDialog.Builder(requireActivity())
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.title_delete_dish))
        //set message for alert dialog
        builder.setMessage(resources.getString(R.string.msg_delete_dish_dialog, dish.title))
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.lbl_yes)) { dialogInterface, _ ->
            deleteDish(dish)
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.lbl_no)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    /**
     * A function to launch the custom dialog.
     */
    private fun filterDishesListDialog() {
        mCustomListDialog = Dialog(requireActivity())
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mCustomListDialog.setContentView(binding.root)
        binding.tvTitle.text = resources.getString(R.string.title_select_item_to_filter)
        val dishTypes = Constants.dishTypes()
        dishTypes.add(0, Constants.ALL_ITEMS)
        // Set the LayoutManager that this RecyclerView will use.
        binding.rvList.layoutManager = LinearLayoutManager(requireActivity())
        // Adapter class is initialized and list is passed in the param.
        val adapter = CustomListItemAdapter(
            requireActivity(),
            this@AllDishesFragment,
            dishTypes,
            Constants.FILTER_SELECTION
        )
        // adapter instance is set to the recyclerview to inflate the items.
        binding.rvList.adapter = adapter
        //Start the dialog and display it on screen.
        mCustomListDialog.show()
    }

    /**
     * A function to get the filter item selection and get the list from database accordingly.
     *
     * @param filterItemSelection
     */
    fun filterSelection(filterItemSelection: String) {
        mCustomListDialog.dismiss()
        Log.i("Filter Selection", filterItemSelection)
        if (filterItemSelection == Constants.ALL_ITEMS) {
            mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) { dishes ->
                dishes.let {
                    if (it.isNotEmpty()) {
                        mbindig.rvDishesList.visibility = View.VISIBLE
                        mbindig.tvNoDishesAddedYet.visibility = View.GONE
                        favDishAdapter.dishesList(it)
                    } else {
                        mbindig.rvDishesList.visibility = View.GONE
                        mbindig.tvNoDishesAddedYet.visibility = View.VISIBLE
                    }
                }
            }
        } else {
            mFavDishViewModel.getFilteredList(filterItemSelection)
                .observe(viewLifecycleOwner) { dishes ->
                    dishes.let {
                        if (it.isNotEmpty()) {
                            mbindig.rvDishesList.visibility = View.VISIBLE
                            mbindig.tvNoDishesAddedYet.visibility = View.GONE
                            favDishAdapter.dishesList(it)
                        } else {
                            mbindig.rvDishesList.visibility = View.GONE
                            mbindig.tvNoDishesAddedYet.visibility = View.VISIBLE
                        }
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity){
            (activity as MainActivity?)!!.showButtonNavigationView()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_all_dishes, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.action_filter_dishes -> {
                filterDishesListDialog()
                return true
            }

            R.id.action_add_dish -> {
                //startActivity(Intent(requireActivity(), AddUpdateDishActivity::class.java))
                findNavController().navigate(AllDishesFragmentDirections.actionNavigationAllDishesToAddUpdateDishActivity(null))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
