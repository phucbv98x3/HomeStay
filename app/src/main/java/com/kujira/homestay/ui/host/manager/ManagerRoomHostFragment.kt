package com.kujira.homestay.ui.host.manager

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.kujira.homestay.R
import com.kujira.homestay.data.model.response.AddRoomModel
import com.kujira.homestay.databinding.FragmentManagerRoomHostBinding
import com.kujira.homestay.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_host_main.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_manager_room.*
import kotlinx.android.synthetic.main.fragment_manager_room_host.*


class ManagerRoomHostFragment :
    BaseFragment<ManagerRoomHostViewModel, FragmentManagerRoomHostBinding>(),
    IClick {
    var countChange = 0
    override fun createViewModel(): Class<ManagerRoomHostViewModel> {
        return ManagerRoomHostViewModel::class.java
    }

    override fun getResourceLayout(): Int = R.layout.fragment_manager_room_host

    override fun initView() {
        activity.btn_managerRoom_on_main.setTextColor(context.getColor(R.color.rdc))
        activity.btn_add_room_on_main.setTextColor(context.getColor(R.color.black))
        activity.btn_my_account_on_main.setTextColor(context.getColor(R.color.black))
        activity.linear_on_main_host.visibility = View.VISIBLE
        rcy_manager.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ManagerRoomAdapter(mutableListOf(), this@ManagerRoomHostFragment)
        }

    }

    override fun bindViewModel() {
        viewModel.managerListRoom()
        viewModel.listRoomLiveData.observe(this, {
            (rcy_manager.adapter as ManagerRoomAdapter).setList(it)
        })


    }

    override fun click(addRoomModel: AddRoomModel) {
        val alertDialog = android.app.AlertDialog.Builder(context).create()
        alertDialog.setTitle("Xóa Phòng")
        alertDialog.setMessage("Bạn xác nhận xóa phòng này!")
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, _ ->
            viewModel.removeRoom(addRoomModel)
            dialog.dismiss()
        }
        alertDialog.show()

    }

    override fun longClick(addRoomModel: AddRoomModel) {
        val alertDialog = AlertDialog.Builder(context).create()
        val dialogView = layoutInflater.inflate(R.layout.custom_dilog_edit, null)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setView(dialogView)

        val roomNameDialog = dialogView.findViewById<EditText>(R.id.edt_room_name_dialog).text
        val sRoom = dialogView.findViewById<EditText>(R.id.edt_s_room_dialog).text
        val numberSleep = dialogView.findViewById<EditText>(R.id.edt_sleep_room_dialog).text
        val convenient = dialogView.findViewById<EditText>(R.id.edt_detail_dialog).text
        val convenientAll = dialogView.findViewById<EditText>(R.id.edt_detail_all_dialog).text
        val price = dialogView.findViewById<EditText>(R.id.edt_price_dialog).text
        dialogView.findViewById<Button>(R.id.btn_edit_dialog).setOnClickListener {
            if (roomNameDialog.isNotEmpty() && sRoom.isNotEmpty() && numberSleep.isNotEmpty()
                && convenient.isNotEmpty() && convenientAll.isNotEmpty() && price.isNotEmpty()
            ) {
                val model = AddRoomModel(
                    addRoomModel.id,
                    "",
                    "",
                    roomNameDialog.toString(),
                    sRoom.toString(),
                    numberSleep
                        .toString(),
                    convenient.toString(),
                    convenientAll.toString(),
                    "",
                    "",
                    "",
                    price.toString(),
                    ""
                )
                viewModel.editRoom(model)
                viewModel.listener.observe(this, {
                    if (it == 1 ) {
                        Toast.makeText(context, "Thành Công !", Toast.LENGTH_LONG).show()
                    }

                })
            } else {
                Toast.makeText(context, "Vui long nhập đủ thông tin !", Toast.LENGTH_LONG).show()
            }
        }
        dialogView.findViewById<Button>(R.id.btn_cancel_edit).setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    override fun clickExitRoom(addRoomModel: AddRoomModel) {
        exitRoomOrReport(addRoomModel)
    }

    private fun exitRoomOrReport(addRoomModel: AddRoomModel) {
        viewModel.getIdClient(addRoomModel.id)
        val alertDialog = AlertDialog.Builder(context).create()
        val dialogView = layoutInflater.inflate(R.layout.custom_exit_or_report, null)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setView(dialogView)
        dialogView.findViewById<Button>(R.id.btn_exit_Room).setOnClickListener {
            viewModel.cancelRoom(addRoomModel.id)
            viewModel.listener.observe(this, {
                if (it == 100 ) {
                    Toast.makeText(context, "Thành Công !", Toast.LENGTH_LONG).show()
                }

            })
            alertDialog.dismiss()
        }
        viewModel.idClient.observe(this,{
            it?.let { id->
                dialogView.findViewById<Button>(R.id.btn_report).setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("bundle", id)
                    navigators.navigate(R.id.reportFragment, bundle)
                    alertDialog.dismiss()
                }
            }
        })

        alertDialog.show()
    }

    override fun clickItem(addRoomModel: AddRoomModel) {
        val bundle = Bundle()
        bundle.putParcelable("bundle", addRoomModel)
        navigators.navigate(R.id.detailFragment, bundle)
    }
}