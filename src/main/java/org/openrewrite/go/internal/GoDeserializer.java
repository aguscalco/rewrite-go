package org.openrewrite.go.internal;

import org.openrewrite.go.proto.GoProto;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GoDeserializer {
    
    public GoFile deserialize(GoProto.GoFile proto) {
        return new GoFile(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            Paths.get(proto.getSourcePath()),
            Charset.forName(proto.getCharsetName()),
            proto.getCharsetBomMarked(),
            proto.hasPackageClause() ? deserializePackageClause(proto.getPackageClause()) : null,
            deserializeImportDecls(proto.getImportsList()),
            deserializeDecls(proto.getDeclarationsList()),
            toSpace(proto.getEof()),
            null,
            null
        );
    }
    
    private PackageClause deserializePackageClause(GoProto.PackageClause proto) {
        return new PackageClause(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeIdent(proto.getName())
        );
    }
    
    private List<ImportDecl> deserializeImportDecls(List<GoProto.ImportDecl> protos) {
        List<ImportDecl> result = new ArrayList<>();
        for (GoProto.ImportDecl proto : protos) {
            result.add(deserializeImportDecl(proto));
        }
        return result;
    }
    
    private ImportDecl deserializeImportDecl(GoProto.ImportDecl proto) {
        return new ImportDecl(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeImportSpecs(proto.getSpecsList()),
            proto.getGrouped(),
            toSpace(proto.getEnd())
        );
    }
    
    private List<ImportSpec> deserializeImportSpecs(List<GoProto.ImportSpec> protos) {
        List<ImportSpec> result = new ArrayList<>();
        for (GoProto.ImportSpec proto : protos) {
            result.add(deserializeImportSpec(proto));
        }
        return result;
    }
    
    private ImportSpec deserializeImportSpec(GoProto.ImportSpec proto) {
        return new ImportSpec(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.hasAlias() ? deserializeIdent(proto.getAlias()) : null,
            deserializeBasicLit(proto.getPath())
        );
    }
    
    private List<Decl> deserializeDecls(List<GoProto.Decl> protos) {
        List<Decl> result = new ArrayList<>();
        for (GoProto.Decl proto : protos) {
            Decl decl = deserializeDecl(proto);
            if (decl != null) {
                result.add(decl);
            }
        }
        return result;
    }
    
    private Decl deserializeDecl(GoProto.Decl proto) {
        if (proto.hasFuncDecl()) {
            return deserializeFuncDecl(proto.getFuncDecl());
        } else if (proto.hasGenDecl()) {
            return deserializeGenDecl(proto.getGenDecl());
        }
        throw unsupported("Decl", proto.getDeclCase().name());
    }
    
    private FuncDecl deserializeFuncDecl(GoProto.FuncDecl proto) {
        return new FuncDecl(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.hasRecv() ? deserializeField(proto.getRecv()) : null,
            deserializeIdent(proto.getName()),
            deserializeTypeParamDecls(proto.getTypeParamsList()),
            proto.hasType() ? deserializeFuncType(proto.getType()) : null,
            proto.hasBody() ? deserializeBlockStmt(proto.getBody()) : null
        );
    }
    
    private GenDecl deserializeGenDecl(GoProto.GenDecl proto) {
        return new GenDecl(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.getTok(),
            deserializeSpecs(proto.getSpecsList()),
            proto.getGrouped(),
            toSpace(proto.getEnd())
        );
    }
    
    private List<Spec> deserializeSpecs(List<GoProto.Spec> protos) {
        List<Spec> result = new ArrayList<>();
        for (GoProto.Spec proto : protos) {
            Spec spec = deserializeSpec(proto);
            if (spec != null) {
                result.add(spec);
            }
        }
        return result;
    }
    
    private Spec deserializeSpec(GoProto.Spec proto) {
        if (proto.hasValueSpec()) {
            return deserializeValueSpec(proto.getValueSpec());
        } else if (proto.hasTypeSpec()) {
            return deserializeTypeSpec(proto.getTypeSpec());
        }
        throw unsupported("Spec", proto.getSpecCase().name());
    }
    
    private ValueSpec deserializeValueSpec(GoProto.ValueSpec proto) {
        return new ValueSpec(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeIdents(proto.getNamesList()),
            proto.hasType() ? deserializeExpr(proto.getType()) : null,
            deserializeExprs(proto.getValuesList())
        );
    }
    
    private TypeSpec deserializeTypeSpec(GoProto.TypeSpec proto) {
        return new TypeSpec(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeIdent(proto.getName()),
            deserializeTypeParamDecls(proto.getTypeParamsList()),
            proto.hasType() ? deserializeExpr(proto.getType()) : null,
            proto.getAssign()
        );
    }
    
    private Field deserializeField(GoProto.Field proto) {
        return new Field(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.getNamesList(),
            proto.hasType() ? deserializeExpr(proto.getType()) : null,
            proto.getTag()
        );
    }
    
    private BlockStmt deserializeBlockStmt(GoProto.BlockStmt proto) {
        return new BlockStmt(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeStmts(proto.getStmtsList()),
            toSpace(proto.getEnd())
        );
    }
    
    private List<Stmt> deserializeStmts(List<GoProto.Stmt> protos) {
        List<Stmt> result = new ArrayList<>();
        for (GoProto.Stmt proto : protos) {
            Stmt stmt = deserializeStmt(proto);
            if (stmt != null) {
                result.add(stmt);
            }
        }
        return result;
    }
    
    private Stmt deserializeStmt(GoProto.Stmt proto) {
        if (proto.hasBlockStmt()) {
            return deserializeBlockStmt(proto.getBlockStmt());
        } else if (proto.hasExprStmt()) {
            return new ExprStmt(
                toUUID(proto.getExprStmt().getId()),
                toSpace(proto.getExprStmt().getPrefix()),
                toMarkers(proto.getExprStmt().getMarkers()),
                deserializeExpr(proto.getExprStmt().getExpr())
            );
        } else if (proto.hasAssignStmt()) {
            return deserializeAssignStmt(proto.getAssignStmt());
        } else if (proto.hasReturnStmt()) {
            return deserializeReturnStmt(proto.getReturnStmt());
        } else if (proto.hasIfStmt()) {
            return deserializeIfStmt(proto.getIfStmt());
        }
        throw unsupported("Stmt", proto.getStmtCase().name());
    }
    
    private AssignStmt deserializeAssignStmt(GoProto.AssignStmt proto) {
        return new AssignStmt(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeExprs(proto.getLhsList()),
            proto.getTok(),
            deserializeExprs(proto.getRhsList())
        );
    }
    
    private ReturnStmt deserializeReturnStmt(GoProto.ReturnStmt proto) {
        return new ReturnStmt(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeExprs(proto.getResultsList())
        );
    }
    
    private IfStmt deserializeIfStmt(GoProto.IfStmt proto) {
        return new IfStmt(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.hasInit() ? deserializeStmt(proto.getInit()) : null,
            proto.hasCond() ? deserializeExpr(proto.getCond()) : null,
            proto.hasBody() ? deserializeBlockStmt(proto.getBody()) : null,
            proto.hasElseStmt() ? deserializeStmt(proto.getElseStmt()) : null
        );
    }
    
    private List<Expr> deserializeExprs(List<GoProto.Expr> protos) {
        List<Expr> result = new ArrayList<>();
        for (GoProto.Expr proto : protos) {
            Expr expr = deserializeExpr(proto);
            if (expr != null) {
                result.add(expr);
            }
        }
        return result;
    }
    
    private Expr deserializeExpr(GoProto.Expr proto) {
        if (proto.hasIdent()) {
            return deserializeIdent(proto.getIdent());
        } else if (proto.hasBasicLit()) {
            return deserializeBasicLit(proto.getBasicLit());
        } else if (proto.hasCallExpr()) {
            return deserializeCallExpr(proto.getCallExpr());
        } else if (proto.hasSelectorExpr()) {
            return deserializeSelectorExpr(proto.getSelectorExpr());
        } else if (proto.hasBinaryExpr()) {
            return deserializeBinaryExpr(proto.getBinaryExpr());
        } else if (proto.hasUnaryExpr()) {
            return deserializeUnaryExpr(proto.getUnaryExpr());
        } else if (proto.hasInterfaceTypeExpr()) {
            return deserializeInterfaceTypeExpr(proto.getInterfaceTypeExpr());
        } else if (proto.hasArrayTypeExpr()) {
            return deserializeArrayTypeExpr(proto.getArrayTypeExpr());
        } else if (proto.hasMapTypeExpr()) {
            return deserializeMapTypeExpr(proto.getMapTypeExpr());
        } else if (proto.hasChanTypeExpr()) {
            return deserializeChanTypeExpr(proto.getChanTypeExpr());
        } else if (proto.hasStructTypeExpr()) {
            return deserializeStructTypeExpr(proto.getStructTypeExpr());
        } else if (proto.hasFuncTypeExpr()) {
            return deserializeFuncTypeExpr(proto.getFuncTypeExpr());
        } else if (proto.hasTypeAssertExpr()) {
            return deserializeTypeAssertExpr(proto.getTypeAssertExpr());
        } else if (proto.hasStarExpr()) {
            return deserializeStarExpr(proto.getStarExpr());
        } else if (proto.hasSliceTypeExpr()) {
            return deserializeSliceTypeExpr(proto.getSliceTypeExpr());
        } else if (proto.hasPointerTypeExpr()) {
            return deserializePointerTypeExpr(proto.getPointerTypeExpr());
        }
        throw unsupported("Expr", proto.getExprCase().name());
    }

    private SliceTypeExpr deserializeSliceTypeExpr(GoProto.SliceTypeExpr proto) {
        return new SliceTypeExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.hasElt() ? deserializeExpr(proto.getElt()) : null
        );
    }

    private PointerTypeExpr deserializePointerTypeExpr(GoProto.PointerTypeExpr proto) {
        return new PointerTypeExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.hasBase() ? deserializeExpr(proto.getBase()) : null
        );
    }
    
    private Ident deserializeIdent(GoProto.Ident proto) {
        return new Ident(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.getName(),
            proto.hasType() ? deserializeGoType(proto.getType()) : null
        );
    }
    
    private List<Ident> deserializeIdents(List<GoProto.Ident> protos) {
        List<Ident> result = new ArrayList<>();
        for (GoProto.Ident proto : protos) {
            result.add(deserializeIdent(proto));
        }
        return result;
    }
    
    private BasicLit deserializeBasicLit(GoProto.BasicLit proto) {
        return new BasicLit(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.getKind(),
            proto.getValue()
        );
    }
    
    private CallExpr deserializeCallExpr(GoProto.CallExpr proto) {
        return new CallExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeExpr(proto.getFun()),
            deserializeExprs(proto.getArgsList()),
            proto.getEllipsis(),
            proto.hasType() ? deserializeGoType(proto.getType()) : null
        );
    }
    
    private SelectorExpr deserializeSelectorExpr(GoProto.SelectorExpr proto) {
        return new SelectorExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeExpr(proto.getX()),
            deserializeIdent(proto.getSel()),
            proto.hasType() ? deserializeGoType(proto.getType()) : null
        );
    }
    
    private BinaryExpr deserializeBinaryExpr(GoProto.BinaryExpr proto) {
        return new BinaryExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeExpr(proto.getX()),
            proto.getOp(),
            deserializeExpr(proto.getY()),
            proto.hasType() ? deserializeGoType(proto.getType()) : null
        );
    }
    
    private UnaryExpr deserializeUnaryExpr(GoProto.UnaryExpr proto) {
        return new UnaryExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.getOp(),
            deserializeExpr(proto.getX()),
            proto.hasType() ? deserializeGoType(proto.getType()) : null
        );
    }
    
    private InterfaceTypeExpr deserializeInterfaceTypeExpr(GoProto.InterfaceTypeExpr proto) {
        return new InterfaceTypeExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeMethods(proto.getMethodsList())
        );
    }
    
    private List<Method> deserializeMethods(List<GoProto.Method> protos) {
        List<Method> result = new ArrayList<>();
        for (GoProto.Method proto : protos) {
            result.add(deserializeMethod(proto));
        }
        return result;
    }
    
    private Method deserializeMethod(GoProto.Method proto) {
        return new Method(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.getName(),
            proto.hasType() ? deserializeFuncType(proto.getType()) : null
        );
    }
    
    private ArrayTypeExpr deserializeArrayTypeExpr(GoProto.ArrayTypeExpr proto) {
        return new ArrayTypeExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.hasLen() ? deserializeExpr(proto.getLen()) : null,
            proto.hasElt() ? deserializeExpr(proto.getElt()) : null
        );
    }
    
    private MapTypeExpr deserializeMapTypeExpr(GoProto.MapTypeExpr proto) {
        return new MapTypeExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.hasKey() ? deserializeExpr(proto.getKey()) : null,
            proto.hasValue() ? deserializeExpr(proto.getValue()) : null
        );
    }
    
    private ChanTypeExpr deserializeChanTypeExpr(GoProto.ChanTypeExpr proto) {
        return new ChanTypeExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.getDir(),
            proto.hasValue() ? deserializeExpr(proto.getValue()) : null
        );
    }
    
    private StructTypeExpr deserializeStructTypeExpr(GoProto.StructTypeExpr proto) {
        return new StructTypeExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeFields(proto.getFieldsList())
        );
    }
    
    private FuncTypeExpr deserializeFuncTypeExpr(GoProto.FuncTypeExpr proto) {
        return new FuncTypeExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeFields(proto.getParamsList()),
            deserializeFields(proto.getResultsList())
        );
    }
    
    private TypeAssertExpr deserializeTypeAssertExpr(GoProto.TypeAssertExpr proto) {
        return new TypeAssertExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.hasX() ? deserializeExpr(proto.getX()) : null,
            proto.hasType() ? deserializeExpr(proto.getType()) : null,
            proto.hasResolvedType() ? deserializeGoType(proto.getResolvedType()) : null
        );
    }
    
    private StarExpr deserializeStarExpr(GoProto.StarExpr proto) {
        return new StarExpr(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            proto.hasX() ? deserializeExpr(proto.getX()) : null,
            proto.hasType() ? deserializeGoType(proto.getType()) : null
        );
    }
    
    private FuncType deserializeFuncType(GoProto.FuncType proto) {
        return new FuncType(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            deserializeFields(proto.getParamsList()),
            deserializeFields(proto.getResultsList()),
            deserializeTypeParamDecls(proto.getTypeParamsList())
        );
    }
    
    private List<Field> deserializeFields(List<GoProto.Field> protos) {
        List<Field> result = new ArrayList<>();
        for (GoProto.Field proto : protos) {
            result.add(deserializeField(proto));
        }
        return result;
    }
    
    private List<TypeParamDecl> deserializeTypeParamDecls(List<GoProto.TypeParamDecl> protos) {
        List<TypeParamDecl> result = new ArrayList<>();
        for (GoProto.TypeParamDecl proto : protos) {
            result.add(new TypeParamDecl(
                toUUID(proto.getId()),
                toSpace(proto.getPrefix()),
                toMarkers(proto.getMarkers()),
                proto.getNamesList(),
                proto.hasConstraint() ? deserializeGoType(proto.getConstraint()) : null
            ));
        }
        return result;
    }
    
    private GoType deserializeGoType(GoProto.GoType proto) {
        GoType.Type type = null;
        
        if (proto.hasBasic()) {
            type = new GoType.Basic(proto.getBasic().getKind());
        } else if (proto.hasNamed()) {
            type = new GoType.Named(
                proto.getNamed().getPackagePath(),
                proto.getNamed().getName(),
                proto.getNamed().hasUnderlying() ? deserializeGoType(proto.getNamed().getUnderlying()) : null
            );
        } else if (proto.hasPointer()) {
            type = new GoType.Pointer(deserializeGoType(proto.getPointer().getElem()));
        } else if (proto.hasSlice()) {
            type = new GoType.Slice(deserializeGoType(proto.getSlice().getElem()));
        } else if (proto.hasArray()) {
            type = new GoType.Array(
                proto.getArray().getLen(),
                deserializeGoType(proto.getArray().getElem())
            );
        } else if (proto.hasMap()) {
            type = new GoType.Map(
                deserializeGoType(proto.getMap().getKey()),
                deserializeGoType(proto.getMap().getValue())
            );
        }
        
        return new GoType(
            toUUID(proto.getId()),
            toSpace(proto.getPrefix()),
            toMarkers(proto.getMarkers()),
            type
        );
    }
    
    private IllegalArgumentException unsupported(String category, String protoCase) {
        return new IllegalArgumentException(
            "Unsupported Go " + category + " kind '" + protoCase + "'. The proto schema defines it but " +
            "org.openrewrite.go.tree has no corresponding node. Dropping it would corrupt the LST.");
    }
    
    private UUID toUUID(GoProto.UUID proto) {
        return new UUID(proto.getMostSigBits(), proto.getLeastSigBits());
    }
    
    private Space toSpace(GoProto.Space proto) {
        if (proto == null || proto.getWhitespace().isEmpty()) {
            return Space.EMPTY;
        }
        return Space.build(proto.getWhitespace());
    }
    
    private Markers toMarkers(GoProto.Markers proto) {
        if (proto == null) {
            return Markers.EMPTY;
        }
        return new Markers(toUUID(proto.getId()), java.util.Collections.emptyList());
    }
}
