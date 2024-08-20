	.data
	.balign 8
QueensStatic.row:
	.zero 64
	.data
	.balign 8
QueensStatic.col:
	.zero 64
	.data
	.balign 8
QueensStatic.diag1:
	.zero 120
	.data
	.balign 8
QueensStatic.diag2:
	.zero 120
	.text
QueensStatic.Solve:
	pushq %rbp
	movq %rsp,%rbp
	subq $48,%rsp
	movq %r14, -32(%rbp)
	movq %r13, -24(%rbp)
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.35:
	movq $8,%rax
	cmpq %rax,%rbx
	je L.1
L.2:
	xorq %rax,%rax
	movq %rax,%r14
	movq $7,%rax
	movq %rax,%r12
	movq $1,%rax
	movq %rax,%r13
L.4:
#	movq %r14,%r14
	leaq QueensStatic.row(%rip),%rax
	movq %r14,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je L.10
L.8:
	movq %r14,%rax
	addq %r13,%rax
	movq %rax,%r14
L.5:
	cmpq %r12,%r14
	jle L.4
L.6:
L.3:
	jmp L.0
L.1:
	call QueensStatic.Print
#	movq %rax,%rax
	jmp L.3
L.10:
	movq %r14,%rax
	addq %rbx,%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl QueensStatic.Solve.badSub
L.11:
	movq $14,%rax
	cmpq %rax,%rcx
	jg QueensStatic.Solve.badSub
L.12:
	leaq QueensStatic.diag1(%rip),%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jne L.8
L.9:
	movq $8,%rax
	movq %rax,%rcx
	subq $1,%rcx
	movq %r14,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	subq %rbx,%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl QueensStatic.Solve.badSub
L.13:
	movq $14,%rax
	cmpq %rax,%rcx
	jg QueensStatic.Solve.badSub
L.14:
	leaq QueensStatic.diag2(%rip),%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jne L.8
L.7:
	leaq QueensStatic.row(%rip),%rcx
	movq %r14,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	movq %r14,%rax
	addq %rbx,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensStatic.Solve.badSub
L.15:
	movq $14,%rax
	cmpq %rax,%rdx
	jg QueensStatic.Solve.badSub
L.16:
	leaq QueensStatic.diag1(%rip),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	movq $8,%rax
	movq %rax,%rcx
	subq $1,%rcx
	movq %r14,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	subq %rbx,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensStatic.Solve.badSub
L.17:
	movq $14,%rax
	cmpq %rax,%rdx
	jg QueensStatic.Solve.badSub
L.18:
	leaq QueensStatic.diag2(%rip),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl QueensStatic.Solve.badSub
L.19:
	movq $7,%rax
	cmpq %rax,%rbx
	jg QueensStatic.Solve.badSub
L.20:
	leaq QueensStatic.col(%rip),%rax
	movq %rbx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq %r14,0(%rax)
	movq %rbx,%rax
	addq $1,%rax
	movq %rax,%rdi
	call QueensStatic.Solve
#	movq %rax,%rax
	leaq QueensStatic.row(%rip),%rcx
	movq %r14,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	movq %r14,%rax
	addq %rbx,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensStatic.Solve.badSub
L.21:
	movq $14,%rax
	cmpq %rax,%rdx
	jg QueensStatic.Solve.badSub
L.22:
	leaq QueensStatic.diag1(%rip),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	movq $8,%rax
	movq %rax,%rcx
	subq $1,%rcx
	movq %r14,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	subq %rbx,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensStatic.Solve.badSub
L.23:
	movq $14,%rax
	cmpq %rax,%rdx
	jg QueensStatic.Solve.badSub
L.24:
	leaq QueensStatic.diag2(%rip),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	jmp L.8
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	movq -24(%rbp),%r13
	movq -32(%rbp),%r14
	addq $48,%rsp
	popq %rbp
	ret
QueensStatic.Solve.badSub:
	call badSub
