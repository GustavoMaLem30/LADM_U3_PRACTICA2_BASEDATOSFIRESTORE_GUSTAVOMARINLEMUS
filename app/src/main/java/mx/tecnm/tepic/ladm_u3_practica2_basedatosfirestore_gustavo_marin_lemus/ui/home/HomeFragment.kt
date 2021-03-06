package mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_gustavo_marin_lemus.ui.home

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_gustavo_marin_lemus.databinding.FragmentHomeBinding
import java.lang.NullPointerException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var listaidGlobal = ArrayList<String>()
    private var idRequerido = ""
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    val baseRemota=FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        FirebaseFirestore.getInstance().collection("area")
            .addSnapshotListener { query, error ->
                if (error != null) {
                    //ERROR
                    AlertDialog.Builder(requireContext())
                        .setMessage(error.message)
                        .show()
                    return@addSnapshotListener
                }
                listaidGlobal.clear()
                val llenar = ArrayList<String>()
                for (documento in query!!) {
                    var cadena = "${documento.getString("descripcion")}"
                    listaidGlobal.add(documento.id)
                    llenar.add(cadena)
                }

                try {
                    binding.listaDeDatos.adapter =
                        ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_1, llenar)
                    binding.listaDeDatos.setOnItemClickListener { adapterView, view, i, l ->
                        idRequerido = listaidGlobal.get(i).toString()
                        baseRemota.collection("area").document(idRequerido).get()
                            .addOnSuccessListener {
                                binding.editDesc.setText(it.getString("descripcion"))
                                binding.editDivi.setText(it.getString("division"))
                                binding.editEmpleado.setText(
                                    it.getLong("cantidad_empleados").toString()
                                )
                            }
                    }
                }catch (err:NullPointerException){

                }
            }
        binding.btnAgregar.setOnClickListener {
            agregarArea()
        }
        binding.btnBuscar.setOnClickListener{
            buscarArea()
        }
        binding.btnActualiza.setOnClickListener {
            baseRemota.collection("area").document(idRequerido).
            update("descripcion",binding.editDesc.text.toString(),
            "division",binding.editDivi.text.toString(),
            "cantidad_empleados",binding.editEmpleado.text.toString().toInt())
            binding.editDesc.setText("")
            binding.editDivi.setText("")
            binding.editEmpleado.setText("")
            Toast.makeText(requireContext(), "SE REALIZO LA ACTUALIZACI??N CORRECTAMENTE", Toast.LENGTH_LONG).show()
        }
        binding.btnBorrar.setOnClickListener {
            baseRemota.collection("area").document(idRequerido).delete()
                .addOnSuccessListener {
                    binding.editDesc.setText("")
                    binding.editDivi.setText("")
                    binding.editEmpleado.setText("")
                    Toast.makeText(requireContext(), "SE REALIZO LA ELIMINACI??N CORRECTAMENTE", Toast.LENGTH_LONG).show()
                }
        }
        return root
    }
    private fun agregarArea(){
        if(binding.editDesc.text.toString().equals("")||binding.editDivi.text.toString().equals("")
            ||binding.editEmpleado.text.toString().equals("")) {
            Toast.makeText(requireContext(), "INGRESE LOS DATOS REQUERIDOS", Toast.LENGTH_LONG).show()
        }else{
            val insertarDatos = hashMapOf(
                "descripcion" to binding.editDesc.text.toString(),
                "division" to binding.editDivi.text.toString(),
                "cantidad_empleados" to binding.editEmpleado.text.toString().toInt())

            baseRemota.collection("area").add(insertarDatos)
                .addOnSuccessListener {
                    //SE REALIZO LA OPERACI??N DE MANERA ??XITOSA
                    Toast.makeText(requireContext(), "SE AGREG?? DE MANERA ??XITOSA EL ??REA", Toast.LENGTH_LONG).show()
                    binding.editDesc.setText("")
                    binding.editDivi.setText("")
                    binding.editEmpleado.setText("")
                }
                .addOnFailureListener {
                    // HUBO UN FALLO
                    AlertDialog.Builder(requireContext())
                        .setMessage(it.message)
                        .show()
                }
        }
    }
    private fun buscarArea(){
        if(binding.editDesc.text.toString().equals("")){
            if (binding.editDivi.text.toString().equals("")){
                var consulta=baseRemota.collection("area")
                realizarBusqueda(consulta,1)
                Toast.makeText(requireContext(), "MOSTRANDO TODOS LOS DATOS", Toast.LENGTH_LONG).show()
            }else{
                var consulta=baseRemota.collection("area").whereEqualTo("division",binding.editDivi.text.toString())
                realizarBusqueda(consulta,0)
                Toast.makeText(requireContext(), "MOSTRANDO DATOS POR DIVISI??N", Toast.LENGTH_LONG).show()
            }
        }else{
            var consulta=baseRemota.collection("area").whereEqualTo("descripcion",binding.editDesc.text.toString())
            realizarBusqueda(consulta,1)
            Toast.makeText(requireContext(), "MOSTRANDO DATOS POR DESCRIPCI??N", Toast.LENGTH_LONG).show()
        }
    }
    fun realizarBusqueda(queryy : Query,i : Int) {
        queryy.addSnapshotListener { query, error ->
            if (error != null) {
                //ERROR
                AlertDialog.Builder(requireContext())
                    .setMessage(error.message)
                    .show()
                return@addSnapshotListener
            }
            listaidGlobal.clear()
            val llenar = ArrayList<String>()
            for (documento in query!!) {
                if (i == 0) {
                    var cadena = "${documento.getString("division")}"
                    llenar.add(cadena)
                    listaidGlobal.add(documento.id)
                } else {
                    var cadena = "${documento.getString("descripcion")}"
                    llenar.add(cadena)
                    listaidGlobal.add(documento.id)
                }
            }
            binding.listaDeDatos.adapter =
                ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_1, llenar)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}