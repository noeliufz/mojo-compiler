	.data
	.balign 8
QueensOpen.Queens:
	.quad QueensOpen.Init
	.quad QueensOpen.Solve
	.text
QueensOpen.Init:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.94:
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je QueensOpen.Init.badPtr
L.1:
	movq $8,%rax
	movq %rax,%r12
	xorq %rax,%rax
	cmpq %rax,%r12
	jl QueensOpen.Init.badSub
L.2:
	movq %r12,%rcx
	movq $16,%rax
#	movq %rax,%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call malloc
#	movq %rax,%rax
	movq %rax,%rcx
	addq $16,%rcx
	movq %rcx,0(%rax)
	movq %r12,8(%rax)
	movq 8(%rax),%rcx
	movq %rcx,%r8
	movq 0(%rax),%rcx
	movq %rcx,%rdi
	xorq %rcx,%rcx
	movq %rcx,%rsi
	cmpq %r8,%rsi
	jge L.5
L.3:
	movq %rsi,%rcx
	imulq $8,%rcx
	movq %rdi,%rdx
	addq %rcx,%rdx
	xorq %rcx,%rcx
	movq %rcx,0(%rdx)
	movq %rsi,%rcx
	addq $1,%rcx
	movq %rcx,%rsi
L.4:
	cmpq %r8,%rsi
	jl L.3
L.5:
	movq %rax,0(%rbx)
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je QueensOpen.Init.badPtr
L.6:
	movq $8,%rax
	movq %rax,%r12
	xorq %rax,%rax
	cmpq %rax,%r12
	jl QueensOpen.Init.badSub
L.7:
	movq %r12,%rcx
	movq $16,%rax
#	movq %rax,%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call malloc
#	movq %rax,%rax
	movq %rax,%rcx
	addq $16,%rcx
	movq %rcx,0(%rax)
	movq %r12,8(%rax)
	movq 8(%rax),%rcx
	movq %rcx,%r8
	movq 0(%rax),%rcx
	movq %rcx,%rdi
	xorq %rcx,%rcx
	movq %rcx,%rsi
	cmpq %r8,%rsi
	jge L.10
L.8:
	movq %rsi,%rcx
	imulq $8,%rcx
	movq %rdi,%rdx
	addq %rcx,%rdx
	xorq %rcx,%rcx
	movq %rcx,0(%rdx)
	movq %rsi,%rcx
	addq $1,%rcx
	movq %rcx,%rsi
L.9:
	cmpq %r8,%rsi
	jl L.8
L.10:
	movq %rax,8(%rbx)
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je QueensOpen.Init.badPtr
L.11:
	movq $8,%rax
#	movq %rax,%rax
	addq $8,%rax
#	movq %rax,%rax
	subq $1,%rax
	movq %rax,%r12
	xorq %rax,%rax
	cmpq %rax,%r12
	jl QueensOpen.Init.badSub
L.12:
	movq %r12,%rcx
	movq $16,%rax
#	movq %rax,%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call malloc
#	movq %rax,%rax
	movq %rax,%rcx
	addq $16,%rcx
	movq %rcx,0(%rax)
	movq %r12,8(%rax)
	movq 8(%rax),%rcx
	movq %rcx,%r8
	movq 0(%rax),%rcx
	movq %rcx,%rdi
	xorq %rcx,%rcx
	movq %rcx,%rsi
	cmpq %r8,%rsi
	jge L.15
L.13:
	movq %rsi,%rcx
	imulq $8,%rcx
	movq %rdi,%rdx
	addq %rcx,%rdx
	xorq %rcx,%rcx
	movq %rcx,0(%rdx)
	movq %rsi,%rcx
	addq $1,%rcx
	movq %rcx,%rsi
L.14:
	cmpq %r8,%rsi
	jl L.13
L.15:
	movq %rax,16(%rbx)
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je QueensOpen.Init.badPtr
L.16:
	movq $8,%rax
#	movq %rax,%rax
	addq $8,%rax
#	movq %rax,%rax
	subq $1,%rax
	movq %rax,%r12
	xorq %rax,%rax
	cmpq %rax,%r12
	jl QueensOpen.Init.badSub
L.17:
	movq %r12,%rcx
	movq $16,%rax
#	movq %rax,%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call malloc
#	movq %rax,%rax
	movq %rax,%rcx
	addq $16,%rcx
	movq %rcx,0(%rax)
	movq %r12,8(%rax)
	movq 8(%rax),%rcx
	movq %rcx,%r8
	movq 0(%rax),%rcx
	movq %rcx,%rdi
	xorq %rcx,%rcx
	movq %rcx,%rsi
	cmpq %r8,%rsi
	jge L.20
L.18:
	movq %rsi,%rcx
	imulq $8,%rcx
	movq %rdi,%rdx
	addq %rcx,%rdx
	xorq %rcx,%rcx
	movq %rcx,0(%rdx)
	movq %rsi,%rcx
	addq $1,%rcx
	movq %rcx,%rsi
L.19:
	cmpq %r8,%rsi
	jl L.18
L.20:
	movq %rax,24(%rbx)
	movq %rbx,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	addq $32,%rsp
	popq %rbp
	ret
QueensOpen.Init.badPtr:
	call badPtr
QueensOpen.Init.badSub:
	call badSub
	.text
QueensOpen.Solve:
	pushq %rbp
	movq %rsp,%rbp
	subq $64,%rsp
	movq %r15, -40(%rbp)
	movq %r14, -32(%rbp)
	movq %r13, -24(%rbp)
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
	movq %rsi,%r13
	movq %rdi,%r14
L.95:
	movq %r14,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je QueensOpen.Solve.badPtr
L.25:
	movq 8(%rbx),%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je QueensOpen.Solve.badPtr
L.26:
	movq 8(%rbx),%rax
	cmpq %rax,%r13
	je L.22
L.23:
	xorq %rax,%rax
	movq %rax,%r15
	movq %r14,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je QueensOpen.Solve.badPtr
L.27:
	movq 0(%rbx),%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je QueensOpen.Solve.badPtr
L.28:
	movq 8(%rbx),%rax
#	movq %rax,%rax
	subq $1,%rax
	movq %rax,%rbx
	movq $1,%rax
	movq %rax,%r12
L.30:
	cmpq %rbx,%r15
	jle L.29
L.31:
L.24:
	jmp L.21
L.22:
	movq %r14,%rdi
	call QueensOpen.Print
#	movq %rax,%rax
	jmp L.24
L.29:
#	movq %r15,%r15
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.36:
	movq 0(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.37:
	movq %rcx,%rdx
	movq %r15,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl QueensOpen.Solve.badSub
L.38:
	movq 8(%rdx),%rax
	cmpq %rax,%rcx
	jge QueensOpen.Solve.badSub
L.39:
	movq 0(%rdx),%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je L.35
L.33:
	movq %r15,%rax
	addq %r12,%rax
	movq %rax,%r15
	jmp L.30
L.35:
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.40:
	movq 16(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.41:
	movq %rcx,%rdx
	movq %r15,%rax
	addq %r13,%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl QueensOpen.Solve.badSub
L.42:
	movq 8(%rdx),%rax
	cmpq %rax,%rcx
	jge QueensOpen.Solve.badSub
L.43:
	movq 0(%rdx),%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jne L.33
L.34:
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.44:
	movq 24(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.45:
	movq %rcx,%rdx
	movq $8,%rax
	movq %rax,%rcx
	subq $1,%rcx
	movq %r15,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	subq %r13,%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl QueensOpen.Solve.badSub
L.46:
	movq 8(%rdx),%rax
	cmpq %rax,%rcx
	jge QueensOpen.Solve.badSub
L.47:
	movq 0(%rdx),%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jne L.33
L.32:
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.48:
	movq 0(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.49:
#	movq %rcx,%rcx
	movq %r15,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensOpen.Solve.badSub
L.50:
	movq 8(%rcx),%rax
	cmpq %rax,%rdx
	jge QueensOpen.Solve.badSub
L.51:
	movq 0(%rcx),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.52:
	movq 16(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.53:
#	movq %rcx,%rcx
	movq %r15,%rax
	addq %r13,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensOpen.Solve.badSub
L.54:
	movq 8(%rcx),%rax
	cmpq %rax,%rdx
	jge QueensOpen.Solve.badSub
L.55:
	movq 0(%rcx),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.56:
	movq 24(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.57:
	movq %rcx,%rsi
	movq $8,%rax
	movq %rax,%rcx
	subq $1,%rcx
	movq %r15,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	subq %r13,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensOpen.Solve.badSub
L.58:
	movq 8(%rsi),%rax
	cmpq %rax,%rdx
	jge QueensOpen.Solve.badSub
L.59:
	movq 0(%rsi),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.60:
	movq 8(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.61:
	movq %rcx,%rdx
	movq %r13,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl QueensOpen.Solve.badSub
L.62:
	movq 8(%rdx),%rax
	cmpq %rax,%rcx
	jge QueensOpen.Solve.badSub
L.63:
	movq 0(%rdx),%rax
#	movq %rcx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq %r15,0(%rax)
	movq -8(%r14),%rax
	movq 8(%rax),%rcx
	movq %r14,%rdi
	movq %r13,%rax
	addq $1,%rax
	movq %rax,%rsi
	call *%rcx
#	movq %rax,%rax
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.64:
	movq 0(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.65:
#	movq %rcx,%rcx
	movq %r15,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensOpen.Solve.badSub
L.66:
	movq 8(%rcx),%rax
	cmpq %rax,%rdx
	jge QueensOpen.Solve.badSub
L.67:
	movq 0(%rcx),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.68:
	movq 16(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.69:
#	movq %rcx,%rcx
	movq %r15,%rax
	addq %r13,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensOpen.Solve.badSub
L.70:
	movq 8(%rcx),%rax
	cmpq %rax,%rdx
	jge QueensOpen.Solve.badSub
L.71:
	movq 0(%rcx),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.72:
	movq 24(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je QueensOpen.Solve.badPtr
L.73:
	movq %rcx,%rsi
	movq $8,%rax
	movq %rax,%rcx
	subq $1,%rcx
	movq %r15,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	subq %r13,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	jl QueensOpen.Solve.badSub
L.74:
	movq 8(%rsi),%rax
	cmpq %rax,%rdx
	jge QueensOpen.Solve.badSub
L.75:
	movq 0(%rsi),%rcx
	movq %rdx,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	jmp L.33
L.21:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	movq -24(%rbp),%r13
	movq -32(%rbp),%r14
	movq -40(%rbp),%r15
	addq $64,%rsp
	popq %rbp
	ret
QueensOpen.Solve.badPtr:
	call badPtr
QueensOpen.Solve.badSub:
	call badSub
