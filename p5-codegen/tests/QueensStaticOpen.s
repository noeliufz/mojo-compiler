	.data
	.balign 8
QueensStaticOpen.row:
	.zero 8
	.data
	.balign 8
QueensStaticOpen.col:
	.zero 8
	.data
	.balign 8
QueensStaticOpen.diag1:
	.zero 8
	.data
	.balign 8
QueensStaticOpen.diag2:
	.zero 8
	.text
QueensStaticOpen.Solve:
	pushq %rbp
	movq %rsp,%rbp
	subq $48,%rsp
	movq %r14, -32(%rbp)
	movq %r13, -24(%rbp)
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.74:
	leaq QueensStaticOpen.col(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.4:
	movq 8(%rcx),%rax
	cmpq %rax,%rbx
	je L.1
L.2:
	xorq %rax,%rax
	movq %rax,%r14
	leaq QueensStaticOpen.row(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.5:
	movq 8(%rcx),%rax
#	movq %rax,%rax
	subq $1,%rax
	movq %rax,%r12
	movq $1,%rax
	movq %rax,%r13
L.7:
	cmpq %r12,%r14
	jle L.6
L.8:
L.3:
	jmp L.0
L.1:
	call QueensStaticOpen.Print
#	movq %rax,%rax
	jmp L.3
L.6:
#	movq %r14,%r14
	leaq QueensStaticOpen.row(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.13:
	movq %rcx,%rdx
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl QueensStaticOpen.Solve.badSub
L.14:
	movq 8(%rdx),%rax
	cmpq %rax,%rcx
	jge QueensStaticOpen.Solve.badSub
L.15:
	movq 0(%rdx),%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je L.12
L.10:
	movq %r14,%rax
	addq %r13,%rax
	movq %rax,%r14
	jmp L.7
L.12:
	leaq QueensStaticOpen.diag1(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.16:
	movq %rcx,%rdx
	movq %r14,%rax
	addq %rbx,%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl QueensStaticOpen.Solve.badSub
L.17:
	movq 8(%rdx),%rax
	cmpq %rax,%rcx
	jge QueensStaticOpen.Solve.badSub
L.18:
	movq 0(%rdx),%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jne L.10
L.11:
	leaq QueensStaticOpen.diag2(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.19:
	movq %rcx,%rdx
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
	jl QueensStaticOpen.Solve.badSub
L.20:
	movq 8(%rdx),%rax
	cmpq %rax,%rcx
	jge QueensStaticOpen.Solve.badSub
L.21:
	movq 0(%rdx),%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jne L.10
L.9:
	leaq QueensStaticOpen.row(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.22:
#	movq %rcx,%rcx
	movq %r14,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensStaticOpen.Solve.badSub
L.23:
	movq 8(%rcx),%rax
	cmpq %rax,%rdx
	jge QueensStaticOpen.Solve.badSub
L.24:
	movq 0(%rcx),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	leaq QueensStaticOpen.diag1(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.25:
#	movq %rcx,%rcx
	movq %r14,%rax
	addq %rbx,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensStaticOpen.Solve.badSub
L.26:
	movq 8(%rcx),%rax
	cmpq %rax,%rdx
	jge QueensStaticOpen.Solve.badSub
L.27:
	movq 0(%rcx),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	leaq QueensStaticOpen.diag2(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.28:
	movq %rcx,%rsi
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
	jl QueensStaticOpen.Solve.badSub
L.29:
	movq 8(%rsi),%rax
	cmpq %rax,%rdx
	jge QueensStaticOpen.Solve.badSub
L.30:
	movq 0(%rsi),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	leaq QueensStaticOpen.col(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.31:
#	movq %rcx,%rcx
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl QueensStaticOpen.Solve.badSub
L.32:
	movq 8(%rcx),%rax
	cmpq %rax,%rbx
	jge QueensStaticOpen.Solve.badSub
L.33:
	movq 0(%rcx),%rax
	movq %rbx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq %r14,0(%rax)
	movq %rbx,%rax
	addq $1,%rax
	movq %rax,%rdi
	call QueensStaticOpen.Solve
#	movq %rax,%rax
	leaq QueensStaticOpen.row(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.34:
#	movq %rcx,%rcx
	movq %r14,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensStaticOpen.Solve.badSub
L.35:
	movq 8(%rcx),%rax
	cmpq %rax,%rdx
	jge QueensStaticOpen.Solve.badSub
L.36:
	movq 0(%rcx),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	leaq QueensStaticOpen.diag1(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.37:
#	movq %rcx,%rcx
	movq %r14,%rax
	addq %rbx,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensStaticOpen.Solve.badSub
L.38:
	movq 8(%rcx),%rax
	cmpq %rax,%rdx
	jge QueensStaticOpen.Solve.badSub
L.39:
	movq 0(%rcx),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	leaq QueensStaticOpen.diag2(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensStaticOpen.Solve.badPtr
L.40:
	movq %rcx,%rsi
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
	jl QueensStaticOpen.Solve.badSub
L.41:
	movq 8(%rsi),%rax
	cmpq %rax,%rdx
	jge QueensStaticOpen.Solve.badSub
L.42:
	movq 0(%rsi),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	jmp L.10
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	movq -24(%rbp),%r13
	movq -32(%rbp),%r14
	addq $48,%rsp
	popq %rbp
	ret
QueensStaticOpen.Solve.badPtr:
	call badPtr
QueensStaticOpen.Solve.badSub:
	call badSub
