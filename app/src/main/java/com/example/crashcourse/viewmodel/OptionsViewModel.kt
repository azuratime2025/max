package com.example.crashcourse.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crashcourse.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class OptionsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val classOptionDao = database.classOptionDao()
    private val subClassOptionDao = database.subClassOptionDao()
    private val gradeOptionDao = database.gradeOptionDao()
    private val subGradeOptionDao = database.subGradeOptionDao()
    private val programOptionDao = database.programOptionDao()
    private val roleOptionDao = database.roleOptionDao()

    val classOptions: Flow<List<ClassOption>> = classOptionDao.getAllOptions()
    val subClassOptions: Flow<List<SubClassOption>> = subClassOptionDao.getAllOptions()
    val gradeOptions: Flow<List<GradeOption>> = gradeOptionDao.getAllOptions()
    val subGradeOptions: Flow<List<SubGradeOption>> = subGradeOptionDao.getAllOptions()
    val programOptions: Flow<List<ProgramOption>> = programOptionDao.getAllOptions()
    val roleOptions: Flow<List<RoleOption>> = roleOptionDao.getAllOptions()

    fun addOption(optionType: String, name: String, displayOrder: Int, parentId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            when (optionType) {
                "Class" -> {
                    val maxId = classOptionDao.getMaxId() ?: 0
                    val newOption = ClassOption(id = maxId + 1, name, displayOrder)
                    classOptionDao.insert(newOption)
                }

                "SubClass" -> parentId?.let {
                    val maxId = subClassOptionDao.getMaxId() ?: 0
                    val newOption = SubClassOption(id = maxId + 1, name, it, displayOrder)
                    subClassOptionDao.insert(newOption)
                }

                "Grade" -> {
                    val maxId = gradeOptionDao.getMaxId() ?: 0
                    val newOption = GradeOption(id = maxId + 1, name, displayOrder)
                    gradeOptionDao.insert(newOption)
                }

                "SubGrade" -> parentId?.let {
                    val maxId = subGradeOptionDao.getMaxId() ?: 0
                    val newOption = SubGradeOption(id = maxId + 1, name, it, displayOrder)
                    subGradeOptionDao.insert(newOption)
                }

                "Program" -> {
                    val maxId = programOptionDao.getMaxId() ?: 0
                    val newOption = ProgramOption(id = maxId + 1, name, displayOrder)
                    programOptionDao.insert(newOption)
                }

                "Role" -> {
                    val maxId = roleOptionDao.getMaxId() ?: 0
                    val newOption = RoleOption(id = maxId + 1, name, displayOrder)
                    roleOptionDao.insert(newOption)
                }
            }
        }
    }

    fun updateOption(optionType: String, option: Any, name: String, displayOrder: Int, parentId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            when (optionType) {
                "Class" -> if (option is ClassOption) {
                    classOptionDao.update(option.copy(name = name, displayOrder = displayOrder))
                }

                "SubClass" -> if (option is SubClassOption && parentId != null) {
                    subClassOptionDao.update(option.copy(name = name, displayOrder = displayOrder, parentClassId = parentId))
                }

                "Grade" -> if (option is GradeOption) {
                    gradeOptionDao.update(option.copy(name = name, displayOrder = displayOrder))
                }

                "SubGrade" -> if (option is SubGradeOption && parentId != null) {
                    subGradeOptionDao.update(option.copy(name = name, displayOrder = displayOrder, parentGradeId = parentId))
                }

                "Program" -> if (option is ProgramOption) {
                    programOptionDao.update(option.copy(name = name, displayOrder = displayOrder))
                }

                "Role" -> if (option is RoleOption) {
                    roleOptionDao.update(option.copy(name = name, displayOrder = displayOrder))
                }
            }
        }
    }

    fun deleteOption(optionType: String, option: Any) {
        viewModelScope.launch(Dispatchers.IO) {
            when (optionType) {
                "Class" -> if (option is ClassOption) classOptionDao.delete(option)
                "SubClass" -> if (option is SubClassOption) subClassOptionDao.delete(option)
                "Grade" -> if (option is GradeOption) gradeOptionDao.delete(option)
                "SubGrade" -> if (option is SubGradeOption) subGradeOptionDao.delete(option)
                "Program" -> if (option is ProgramOption) programOptionDao.delete(option)
                "Role" -> if (option is RoleOption) roleOptionDao.delete(option)
            }
        }
    }
}
