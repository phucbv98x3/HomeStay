package com.kujira.homestay.ui.host.add

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.kujira.homestay.R
import com.kujira.homestay.data.model.response.AddRoomModel
import com.kujira.homestay.data.model.response.ProvinceFB
import com.kujira.homestay.databinding.FragmentAddRoomHostBinding
import com.kujira.homestay.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_host_main.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_room_host.*
import kotlinx.coroutines.runBlocking


class AddRoomFragment : BaseFragment<AddRoomViewModel, FragmentAddRoomHostBinding>() {
    private var listProvinces = arrayListOf<String>()
    private var listDistrict = arrayListOf<String>()
    private var nameProvince = ""
    private var nameDistricts = ""
    private var nameTypeRoom = ""
    private var uriImg1: Uri? = null
    private var uriImg2: Uri? = null
    private var listProvincesFB = mutableListOf<ProvinceFB>()
    override fun createViewModel(): Class<AddRoomViewModel> {
        return AddRoomViewModel::class.java
    }

    override fun getResourceLayout(): Int = R.layout.fragment_add_room_host

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        activity.btn_managerRoom_on_main.setTextColor(context.getColor(R.color.black))
        activity.btn_add_room_on_main.setTextColor(context.getColor(R.color.rdc))
        activity.btn_my_account_on_main.setTextColor(context.getColor(R.color.black))

        val arrayAdapterProvinces = ArrayAdapter(
            activity,
            android.R.layout.simple_dropdown_item_1line,
            listProvinces
        )
        dataBinding.spinnerProvince.adapter = arrayAdapterProvinces

        viewModel.getListProvincesFB()
        viewModel.listProvincesFB.observe(this, {
            listProvincesFB = it
            for (element in it) {
                listProvinces.add(element.name)
            }
            arrayAdapterProvinces.notifyDataSetChanged()
        })


        val arrayAdapterDistrict = ArrayAdapter(
            activity,
            android.R.layout.simple_dropdown_item_1line,
            listDistrict
        )
        dataBinding.spinnerDistrictsOnAdd.adapter = arrayAdapterDistrict

        dataBinding.spinnerProvince.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    listDistrict.clear()
                    nameProvince = parent?.getItemAtPosition(position).toString()
                    viewModel.getListDistrictFB(parent?.getItemAtPosition(position).toString())
                    viewModel.listDistrictFBLiveData.observe(this@AddRoomFragment, {

                        it?.let { list ->
                            for (element in list) {
                                listDistrict.add(element.name)
                            }
                        }
                        arrayAdapterDistrict.notifyDataSetChanged()
                        dataBinding.spinnerDistrictsOnAdd.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    nameDistricts = if (listDistrict.size == 0) {
                                        ""
                                    } else {
                                        parent?.getItemAtPosition(position)
                                            .toString()
                                    }

                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }
                            }

                    })
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        actionListener()
        spinnerTypeRoom()

    }

    private fun actionListener() {
        val pr = ProgressDialog(context)
        viewModel.listenerBtnAddHome.observe(this, {
            when (it) {
                AddRoomViewModel.BTN_IMG_1 -> {
                    requestImage(111)
                }
                AddRoomViewModel.BTN_IMG_2 -> {
                    requestImage(222)
                }
                AddRoomViewModel.BTN_IMG_ACCESS -> {
                    if (!viewModel.isCheck()){
                        Toast.makeText(context,getString(R.string.error_isEmpty),Toast.LENGTH_LONG).show()
                    }else{
                        Thread().run {
                            viewModel.putHomeStay(uriImg1, uriImg2)
                        }
                        pr.show()
                        viewModel.listenerSuccess.observe(this, { listener ->
                            if (listener == 1) {
                                dataBinding.btnAccessAll.visibility = View.VISIBLE
                                pr.dismiss()
                            }
                        })
                    }


                }
                AddRoomViewModel.BTN_IMG_ACCESS_ALL -> {
                    val wardAddress = viewModel.textWard.get()
                    val nameRoom = viewModel.nameRoom.get()
                    val sRoom = viewModel.sRoom.get()
                    val numberSleepRoom = viewModel.numberSleepRoom.get()
                    val introduce = viewModel.introduce.get()
                    val textDetail = viewModel.textDetailGT.get()
                    val price = viewModel.price.get()
                    if (wardAddress?.isNotEmpty() == true && nameRoom?.isNotEmpty() == true
                        && sRoom?.isNotEmpty() == true && numberSleepRoom?.isNotEmpty() == true
                        && introduce?.isNotEmpty() == true && textDetail?.isNotEmpty() == true
                        && uriImg1.toString().isNotEmpty() && uriImg2.toString().isNotEmpty()
                    ) {
                        val address =
                            viewModel.textWard.get() + "," + nameDistricts + "," + nameProvince
                        val model = AddRoomModel(
                            "",
                            address,
                            nameTypeRoom,
                            nameRoom,
                            sRoom,
                            numberSleepRoom,
                            textDetail,
                            introduce,
                            viewModel.linkImg1,
                            viewModel.linkImg2,
                            "Trống",
                            price ?: "",
                            viewModel.auth.currentUser!!.uid
                        )
                        runBlocking {
                            viewModel.startPutRoom(model)
                        }

                        viewModel.notifyPut.observe(this, { listener->
                            if (listener == 1) {
                                Toast.makeText(context, "Thành công!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Thất bại !", Toast.LENGTH_LONG).show()

                            }
                        })

                    } else {
                        Toast.makeText(context, "Vui lòng nhập đủ thông tin !", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        })
    }

    private fun requestImage(requestCode: Int) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Picture"
            ), requestCode
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == RESULT_OK) {
            val imageUri = data?.data
            img_1.setImageURI(imageUri)

            uriImg1 = imageUri
            viewModel.linkImg1= imageUri.toString()
        }
        if (requestCode == 222 && resultCode == RESULT_OK) {
            val imageUri = data?.data
            img_2.setImageURI(imageUri)
            uriImg2 = imageUri
            viewModel.linkImg2= imageUri.toString()
        }
    }

    private fun spinnerTypeRoom() {
        val listType = arrayListOf("Nhà Nghỉ", "Biệt Thự", "Nhà Riêng")
        val arrayAdapterTypeRoom = ArrayAdapter(
            activity,
            android.R.layout.simple_dropdown_item_1line,
            listType
        )
        dataBinding.spinnerTypeRoom.adapter = arrayAdapterTypeRoom

        dataBinding.spinnerTypeRoom.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    nameTypeRoom = parent?.getItemAtPosition(position)
                        .toString()

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
    }

    override fun bindViewModel() {

    }

}