import 'package:lucahome_flutter/actions/area.actions.dart';
import 'package:lucahome_flutter/models/area.model.dart';
import 'package:redux/redux.dart';

final areaReducer = combineReducers<List<Area>>([
  new TypedReducer<List<Area>, AreaLoadSuccessful>(_loadSuccessful),
  new TypedReducer<List<Area>, AreaLoadFail>(_loadFailed),
  new TypedReducer<List<Area>, AreaAddSuccessful>(_addSuccessful),
  new TypedReducer<List<Area>, AreaAddFail>(_addFailed),
  new TypedReducer<List<Area>, AreaUpdateSuccessful>(_updateSuccessful),
  new TypedReducer<List<Area>, AreaUpdateFail>(_updateFailed),
  new TypedReducer<List<Area>, AreaDeleteSuccessful>(_deleteSuccessful),
  new TypedReducer<List<Area>, AreaDeleteFail>(_deleteFailed),
]);

List<Area> _loadSuccessful(List<Area> areaList, action) => List.unmodifiable(List.from(action.list));
List<Area> _loadFailed(List<Area> areaList, action) => areaList;

List<Area> _addSuccessful(List<Area> areaList, action) => List.unmodifiable(List.from(areaList)..add(action.area));
List<Area> _addFailed(List<Area> areaList, action) => areaList;

List<Area> _updateSuccessful(List<Area> areaList, action) {
  var modifiableList = List.from(areaList);
  var index = modifiableList.indexWhere((area) => area.name == action.area.name);
  modifiableList.replaceRange(index, index + 1, action.area);
  return List.unmodifiable(modifiableList);
}
List<Area> _updateFailed(List<Area> areaList, action) => areaList;

List<Area> _deleteSuccessful(List<Area> areaList, action) => List.unmodifiable(List.from(areaList)..remove(action.area));
List<Area> _deleteFailed(List<Area> areaList, action) => areaList;
