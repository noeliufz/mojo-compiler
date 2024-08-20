	.data
	.balign 8
Queens.Queens:
	.quad Queens.Init
	.quad Queens.Solve
	.text
Queens.Init:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.70:
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je Queens.Init.badPtr
L.1:
	movq $64,%rax
	movq %rax,%rdi
	call malloc
	movq %rax,%rdi
	movq $8,%rax
	movq %rax,%rsi
	xorq %rax,%rax
	movq %rax,%rdx
	cmpq %rsi,%rdx
	jge L.3
L.2:
	movq %rdx,%rax
	imulq $8,%rax
	movq %rdi,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	movq %rdx,%rax
	addq $1,%rax
	movq %rax,%rdx
	cmpq %rsi,%rdx
	jl L.2
L.3:
	movq %rdi,0(%rbx)
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je Queens.Init.badPtr
L.4:
	movq $64,%rax
	movq %rax,%rdi
	call malloc
	movq %rax,%rdi
	movq $8,%rax
	movq %rax,%rsi
	xorq %rax,%rax
	movq %rax,%rdx
	cmpq %rsi,%rdx
	jge L.6
L.5:
	movq %rdx,%rax
	imulq $8,%rax
	movq %rdi,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	movq %rdx,%rax
	addq $1,%rax
	movq %rax,%rdx
	cmpq %rsi,%rdx
	jl L.5
L.6:
	movq %rdi,8(%rbx)
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je Queens.Init.badPtr
L.7:
	movq $120,%rax
	movq %rax,%rdi
	call malloc
	movq %rax,%rdi
	movq $8,%rax
#	movq %rax,%rax
	addq $8,%rax
#	movq %rax,%rax
	subq $1,%rax
	movq %rax,%rsi
	xorq %rax,%rax
	movq %rax,%rdx
	cmpq %rsi,%rdx
	jge L.9
L.8:
	movq %rdx,%rax
	imulq $8,%rax
	movq %rdi,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	movq %rdx,%rax
	addq $1,%rax
	movq %rax,%rdx
	cmpq %rsi,%rdx
	jl L.8
L.9:
	movq %rdi,16(%rbx)
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je Queens.Init.badPtr
L.10:
	movq $120,%rax
	movq %rax,%rdi
	call malloc
	movq %rax,%rdi
	movq $8,%rax
#	movq %rax,%rax
	addq $8,%rax
#	movq %rax,%rax
	subq $1,%rax
	movq %rax,%rsi
	xorq %rax,%rax
	movq %rax,%rdx
	cmpq %rsi,%rdx
	jge L.12
L.11:
	movq %rdx,%rax
	imulq $8,%rax
	movq %rdi,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	movq %rdx,%rax
	addq $1,%rax
	movq %rax,%rdx
	cmpq %rsi,%rdx
	jl L.11
L.12:
	movq %rdi,24(%rbx)
	movq %rbx,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
Queens.Init.badPtr:
	call badPtr
	.text
Queens.Solve:
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
L.71:
	movq $8,%rax
	cmpq %rax,%r13
	je L.14
L.15:
	xorq %rax,%rax
	movq %rax,%r15
	movq $7,%rax
	movq %rax,%rbx
	movq $1,%rax
	movq %rax,%r12
L.17:
#	movq %r15,%r15
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.24:
	movq 0(%rcx),%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	je Queens.Solve.badPtr
L.25:
	movq %r15,%rcx
	imulq $8,%rcx
	movq %rdx,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je L.23
L.21:
	movq %r15,%rax
	addq %r12,%rax
	movq %rax,%r15
L.18:
	cmpq %rbx,%r15
	jle L.17
L.19:
L.16:
	jmp L.13
L.14:
	movq %r14,%rdi
	call Queens.Print
#	movq %rax,%rax
	jmp L.16
L.23:
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.26:
	movq 16(%rcx),%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	je Queens.Solve.badPtr
L.27:
	movq %r15,%rax
	addq %r13,%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl Queens.Solve.badSub
L.28:
	movq $14,%rax
	cmpq %rax,%rcx
	jg Queens.Solve.badSub
L.29:
#	movq %rcx,%rcx
	imulq $8,%rcx
	movq %rdx,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jne L.21
L.22:
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.30:
	movq 24(%rcx),%rax
#	movq %rax,%rax
	xorq %rcx,%rcx
	cmpq %rcx,%rax
	je Queens.Solve.badPtr
L.31:
	movq $8,%rcx
	movq %rcx,%rdx
	subq $1,%rdx
	movq %r15,%rcx
	addq %rdx,%rcx
#	movq %rcx,%rcx
	subq %r13,%rcx
	movq %rcx,%rdx
	xorq %rcx,%rcx
	cmpq %rcx,%rdx
	jl Queens.Solve.badSub
L.32:
	movq $14,%rcx
	cmpq %rcx,%rdx
	jg Queens.Solve.badSub
L.33:
	movq %rdx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
	movq 0(%rax),%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jne L.21
L.20:
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.34:
	movq 0(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.35:
	movq %r15,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.36:
	movq 16(%rcx),%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	je Queens.Solve.badPtr
L.37:
	movq %r15,%rax
	addq %r13,%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl Queens.Solve.badSub
L.38:
	movq $14,%rax
	cmpq %rax,%rcx
	jg Queens.Solve.badSub
L.39:
	movq %rcx,%rax
	imulq $8,%rax
	movq %rdx,%rcx
	addq %rax,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.40:
	movq 24(%rcx),%rax
#	movq %rax,%rax
	xorq %rcx,%rcx
	cmpq %rcx,%rax
	je Queens.Solve.badPtr
L.41:
	movq $8,%rcx
	movq %rcx,%rdx
	subq $1,%rdx
	movq %r15,%rcx
	addq %rdx,%rcx
#	movq %rcx,%rcx
	subq %r13,%rcx
	movq %rcx,%rdx
	xorq %rcx,%rcx
	cmpq %rcx,%rdx
	jl Queens.Solve.badSub
L.42:
	movq $14,%rcx
	cmpq %rcx,%rdx
	jg Queens.Solve.badSub
L.43:
#	movq %rdx,%rdx
	imulq $8,%rdx
	movq %rax,%rcx
	addq %rdx,%rcx
	movq $1,%rax
	movq %rax,0(%rcx)
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.44:
	movq 8(%rcx),%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	je Queens.Solve.badPtr
L.45:
	movq %r13,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl Queens.Solve.badSub
L.46:
	movq $7,%rax
	cmpq %rax,%rcx
	jg Queens.Solve.badSub
L.47:
#	movq %rcx,%rcx
	imulq $8,%rcx
	movq %rdx,%rax
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
	je Queens.Solve.badPtr
L.48:
	movq 0(%rcx),%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.49:
	movq %r15,%rax
	imulq $8,%rax
#	movq %rcx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.50:
	movq 16(%rcx),%rax
	movq %rax,%rdx
	xorq %rax,%rax
	cmpq %rax,%rdx
	je Queens.Solve.badPtr
L.51:
	movq %r15,%rax
	addq %r13,%rax
	movq %rax,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	jl Queens.Solve.badSub
L.52:
	movq $14,%rax
	cmpq %rax,%rcx
	jg Queens.Solve.badSub
L.53:
	movq %rcx,%rax
	imulq $8,%rax
	movq %rdx,%rcx
	addq %rax,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	movq %r14,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Queens.Solve.badPtr
L.54:
	movq 24(%rcx),%rax
#	movq %rax,%rax
	xorq %rcx,%rcx
	cmpq %rcx,%rax
	je Queens.Solve.badPtr
L.55:
	movq $8,%rcx
	movq %rcx,%rdx
	subq $1,%rdx
	movq %r15,%rcx
	addq %rdx,%rcx
#	movq %rcx,%rcx
	subq %r13,%rcx
	movq %rcx,%rdx
	xorq %rcx,%rcx
	cmpq %rcx,%rdx
	jl Queens.Solve.badSub
L.56:
	movq $14,%rcx
	cmpq %rcx,%rdx
	jg Queens.Solve.badSub
L.57:
#	movq %rdx,%rdx
	imulq $8,%rdx
	movq %rax,%rcx
	addq %rdx,%rcx
	xorq %rax,%rax
	movq %rax,0(%rcx)
	jmp L.21
L.13:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	movq -24(%rbp),%r13
	movq -32(%rbp),%r14
	movq -40(%rbp),%r15
	addq $64,%rsp
	popq %rbp
	ret
Queens.Solve.badPtr:
	call badPtr
Queens.Solve.badSub:
	call badSub
