package parser

import (
	"go/ast"
	"go/parser"
	"go/token"
	"go/types"
	"os"

	"github.com/google/uuid"
	"github.com/openrewrite/rewrite-go/parser/lst"
	"github.com/openrewrite/rewrite-go/parser/proto"
)

type Parser struct {
	fset *token.FileSet
}

func New() *Parser {
	return &Parser{
		fset: token.NewFileSet(),
	}
}

func (p *Parser) ParseFile(filename string) (*proto.GoFile, error) {
	src, err := os.ReadFile(filename)
	if err != nil {
		return nil, err
	}

	return p.ParseSource(filename, src)
}

func (p *Parser) ParseSource(filename string, src []byte) (*proto.GoFile, error) {
	file, err := parser.ParseFile(p.fset, filename, src, parser.ParseComments)
	if err != nil {
		return nil, err
	}

	conf := types.Config{
		Importer: nil,
		Error:    func(err error) {},
	}

	info := &types.Info{
		Types:      make(map[ast.Expr]types.TypeAndValue),
		Defs:       make(map[*ast.Ident]types.Object),
		Uses:       make(map[*ast.Ident]types.Object),
		Implicits:  make(map[ast.Node]types.Object),
		Selections: make(map[*ast.SelectorExpr]*types.Selection),
		Scopes:     make(map[ast.Node]*types.Scope),
	}

	conf.Check("", p.fset, []*ast.File{file}, info)

	builder := lst.NewBuilder(p.fset, info, src)
	return builder.BuildFile(file), nil
}

func (p *Parser) ParsePackage(dir string) ([]*proto.GoFile, error) {
	pkgs, err := parser.ParseDir(p.fset, dir, nil, parser.ParseComments)
	if err != nil {
		return nil, err
	}

	var files []*proto.GoFile
	for _, pkg := range pkgs {
		for filename, file := range pkg.Files {
			conf := types.Config{
				Importer: nil,
				Error:    func(err error) {},
			}

			info := &types.Info{
				Types:      make(map[ast.Expr]types.TypeAndValue),
				Defs:       make(map[*ast.Ident]types.Object),
				Uses:       make(map[*ast.Ident]types.Object),
				Implicits:  make(map[ast.Node]types.Object),
				Selections: make(map[*ast.SelectorExpr]*types.Selection),
				Scopes:     make(map[ast.Node]*types.Scope),
			}

			src, err := os.ReadFile(filename)
			if err != nil {
				continue
			}

			conf.Check("", p.fset, []*ast.File{file}, info)

			builder := lst.NewBuilder(p.fset, info, src)
			goFile := builder.BuildFile(file)
			files = append(files, goFile)
		}
	}

	return files, nil
}

func newUUID() *proto.UUID {
	id := uuid.New()
	return &proto.UUID{
		MostSigBits:  int64(id[0])<<56 | int64(id[1])<<48 | int64(id[2])<<40 | int64(id[3])<<32 |
			int64(id[4])<<24 | int64(id[5])<<16 | int64(id[6])<<8 | int64(id[7]),
		LeastSigBits: int64(id[8])<<56 | int64(id[9])<<48 | int64(id[10])<<40 | int64(id[11])<<32 |
			int64(id[12])<<24 | int64(id[13])<<16 | int64(id[14])<<8 | int64(id[15]),
	}
}
