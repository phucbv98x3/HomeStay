package com.kujira.homestay.ui.host.add

import android.net.Uri
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.kujira.homestay.R
import com.kujira.homestay.data.model.response.AddRoomModel
import com.kujira.homestay.data.model.response.DistrictFB
import com.kujira.homestay.data.model.response.ProvinceFB
import com.kujira.homestay.ui.base.BaseViewModel
import com.kujira.homestay.utils.Constants

class AddRoomViewModel : BaseViewModel() {

    private var listProvinceFB = mutableListOf<ProvinceFB>()
    private var listDistrictsFB = mutableListOf<DistrictFB>()

    var listDistrictFBLiveData = MutableLiveData<MutableList<DistrictFB>>()
    var listProvincesFB = MutableLiveData<MutableList<ProvinceFB>>()
    private var dataReference =
        FirebaseDatabase.getInstance().getReference("Host")
    var auth = FirebaseAuth.getInstance()
    private var dataStoreRef = FirebaseStorage.getInstance().getReference("HostStorage")
    var textWard = ObservableField<String>()
    var nameRoom = ObservableField<String>()
    var sRoom = ObservableField<String>()
    var numberSleepRoom = ObservableField<String>()
    var textDetailGT = ObservableField<String>()
    var introduce = ObservableField<String>()
    var price = ObservableField<String>()
    var listenerBtnAddHome = MutableLiveData<Int>()

    var listenerSuccess = MutableLiveData<Int>()

    var notifyPut = MutableLiveData<Int>()
    var linkImg1 = ""
    var linkImg2 = ""

    companion object {
        const val BTN_IMG_1 = 0
        const val BTN_IMG_2 = 1
        const val BTN_IMG_ACCESS = 2
        const val BTN_IMG_ACCESS_ALL = 3
    }

    fun isCheck(): Boolean {
        return !(textWard.get().toString().isEmpty() || nameRoom.get().toString().isEmpty()
                || sRoom.get().toString().isEmpty() || numberSleepRoom.get().toString().isEmpty()
                || textDetailGT.get().toString().isEmpty() || price.get().toString()
            .isEmpty() || linkImg1.isEmpty() || linkImg2.isEmpty() || introduce.get().toString()
            .isEmpty())
    }

    fun getListProvincesFB() {
        dataReference.child("ListProvinces")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    listProvinceFB.clear()
                    for (pos in snapshot.children) {
                        val obProvinceFB = ProvinceFB(
                            pos.child("code").value.toString().toInt(),
                            pos.child("name").value.toString(),
                            pos.child("phone_code").value.toString().toInt(),
                        )
                        listProvinceFB.add(obProvinceFB)
                    }
                    listProvincesFB.value = listProvinceFB
                }

            })
    }

    fun getListDistrictFB(name: String) {
        listDistrictsFB.clear()
        dataReference.child("DetailProvince").child(name).child("Districts")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (pos in snapshot.children) {
                        val obDistrictFB = DistrictFB(
                            pos.child("code").value.toString(),
                            pos.child("name").value.toString(),

                            )
                        listDistrictsFB.add(obDistrictFB)
                    }
                    listDistrictFBLiveData.value = listDistrictsFB
                }

            })
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_add_img_1 -> {
                listenerBtnAddHome.value = BTN_IMG_1
            }
            R.id.btn_add_img_2 -> {
                listenerBtnAddHome.value = BTN_IMG_2
            }
            R.id.btn_access_add -> {
                listenerBtnAddHome.value = BTN_IMG_ACCESS
            }
            R.id.btn_access_all -> {
                listenerBtnAddHome.value = BTN_IMG_ACCESS_ALL
            }
        }
    }

    fun putHomeStay(img1: Uri?, img2: Uri?) {
        val imgRef = dataStoreRef.child(auth.currentUser!!.uid)
            .child("image")
        val imgName1 = imgRef.child(img1.toString())
        if (img1 != null && img2 != null) {
            imgName1.putFile(img1)
                .addOnSuccessListener {
                    imgName1.downloadUrl.addOnCompleteListener { p0 ->
                        if (p0.isSuccessful) {
                            linkImg1 = p0.result.toString()

                        }
                    }.addOnFailureListener {

                    }
                }


            val imgName2 = imgRef.child(img2.toString())
            imgName2.putFile(img2)
                .addOnSuccessListener {
                    imgName2.downloadUrl.addOnCompleteListener { p0 ->
                        if (p0.isSuccessful) {
                            linkImg2 = p0.result.toString()
                            listenerSuccess.value = 1

                        }
                    }.addOnFailureListener {

                    }
                }
        }

    }

    fun startPutRoom(addRoom: AddRoomModel) {
        if (linkImg2.isNotEmpty() && linkImg1.isNotEmpty()) {
            dataReference.child("ListRoom").child(auth.currentUser!!.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val key = dataReference.push().key.toString()
                        val hashMap = HashMap<String, String>()
                        hashMap["id"] = key
                        hashMap["address"] = addRoom.address
                        hashMap["typeRoom"] = addRoom.typeRoom
                        hashMap["nameRoom"] = addRoom.nameRoom
                        hashMap["s_room"] = addRoom.s_room
                        hashMap["numberSleepRoom"] = addRoom.numberSleepRoom
                        hashMap["convenient"] = addRoom.convenient
                        hashMap["introduce"] = addRoom.introduce
                        hashMap["imageRoom1"] = linkImg1
                        hashMap["imageRoom2"] = linkImg2
                        hashMap["status"] = "Còn Phòng"
                        hashMap["price"] = addRoom.price
                        hashMap["uid"] = addRoom.uid

                        dataReference.child(Constants.LIST_ROOM).child(key).setValue(hashMap)
                            .addOnSuccessListener {
                                notifyPut.value = 1
                            }
                            .addOnFailureListener {
                                notifyPut.value = 0
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

}

