	.text
Append.println:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rax
L.40:
#	movq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je Append.println.badPtr
L.3:
	movq %rbx,%rcx
	xorq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl Append.println.badSub
L.4:
	movq 8(%rcx),%rax
	cmpq %rax,%rbx
	jge Append.println.badSub
L.5:
	movq 0(%rcx),%rax
#	movq %rax,%rax
	addq %rbx,%rax
	movq %rax,%rdi
	call puts
#	movq %rax,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
Append.println.badPtr:
	call badPtr
Append.println.badSub:
	call badSub
	.text
Append.append:
	pushq %rbp
	movq %rsp,%rbp
	subq $48,%rsp
	movq %r15, -40(%rbp)
	movq %r14, -32(%rbp)
	movq %r13, -24(%rbp)
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
	movq %rsi,%rcx
	movq %rdi,%rax
L.41:
	movq %rax,%rbx
	movq %rcx,%r12
#	movq %rbx,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je Append.append.badPtr
L.7:
	movq 8(%rbx),%rax
#	movq %rax,%rax
	subq $1,%rax
	movq %rax,%r14
	movq %r12,%rcx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je Append.append.badPtr
L.8:
	movq 8(%rcx),%rax
#	movq %rax,%rax
	subq $1,%rax
	movq %rax,%r13
	movq %r14,%rax
	addq %r13,%rax
#	movq %rax,%rax
	addq $1,%rax
	movq %rax,%r15
	xorq %rax,%rax
	cmpq %rax,%r15
	jl Append.append.badSub
L.9:
	movq %r15,%rcx
	movq $16,%rax
#	movq %rax,%rax
#	movq %rax,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call malloc
#	movq %rax,%rax
	movq %rax,%rcx
	addq $16,%rcx
	movq %rcx,0(%rax)
	movq %r15,8(%rax)
	movq 8(%rax),%rcx
	movq %rcx,%r8
	movq 0(%rax),%rcx
	movq %rcx,%rdi
	xorq %rcx,%rcx
	movq %rcx,%rsi
	cmpq %r8,%rsi
	jge L.12
L.10:
	movq %rdi,%rdx
	addq %rsi,%rdx
	xorq %rcx,%rcx
	movq %rcx,0(%rdx)
	movq %rsi,%rcx
	addq $1,%rcx
	movq %rcx,%rsi
L.11:
	cmpq %r8,%rsi
	jl L.10
L.12:
#	movq %rax,%rax
	xorq %rcx,%rcx
	movq %rcx,%rsi
	movq %r14,%rcx
	subq $1,%rcx
	movq %rcx,%rdx
	movq $1,%rcx
#	movq %rcx,%rcx
L.14:
	cmpq %rdx,%rsi
	jle L.13
L.15:
	xorq %rbx,%rbx
	movq %rbx,%rdx
	movq %r13,%rbx
	subq $1,%rbx
	movq %rbx,%rcx
	movq $1,%rbx
#	movq %rbx,%rbx
L.23:
	cmpq %rcx,%rdx
	jle L.22
L.24:
#	movq %rax,%rax
	xorq %rbx,%rbx
	cmpq %rbx,%rax
	je Append.append.badPtr
L.31:
	movq %rax,%rcx
	movq %r14,%rbx
	addq %r13,%rbx
	movq %rbx,%rdx
	xorq %rbx,%rbx
	cmpq %rbx,%rdx
	jl Append.append.badSub
L.32:
	movq 8(%rcx),%rbx
	cmpq %rbx,%rdx
	jge Append.append.badSub
L.33:
	movq 0(%rcx),%rbx
	movq %rbx,%rcx
	addq %rdx,%rcx
	xorq %rbx,%rbx
	movq %rbx,0(%rcx)
#	movq %rax,%rax
	jmp L.6
L.13:
#	movq %rsi,%rsi
#	movq %rax,%rax
	xorq %rdi,%rdi
	cmpq %rdi,%rax
	je Append.append.badPtr
L.16:
	movq %rax,%r8
	movq %rsi,%r10
	xorq %rdi,%rdi
	cmpq %rdi,%r10
	jl Append.append.badSub
L.17:
	movq 8(%r8),%rdi
	cmpq %rdi,%r10
	jge Append.append.badSub
L.18:
#	movq %rbx,%rbx
	xorq %rdi,%rdi
	cmpq %rdi,%rbx
	je Append.append.badPtr
L.19:
	movq %rbx,%r9
	movq %rsi,%rdi
	xorq %r11,%r11
	cmpq %r11,%rdi
	jl Append.append.badSub
L.20:
	movq 8(%r9),%r11
	cmpq %r11,%rdi
	jge Append.append.badSub
L.21:
	movq 0(%r8),%r8
#	movq %r8,%r8
	addq %r10,%r8
	movq 0(%r9),%r9
#	movq %r9,%r9
	addq %rdi,%r9
	movsbq 0(%r9),%rdi
	movq %rdi,0(%r8)
#	movq %rsi,%rsi
	addq %rcx,%rsi
#	movq %rsi,%rsi
	jmp L.14
L.22:
#	movq %rdx,%rdx
#	movq %rax,%rax
	xorq %rsi,%rsi
	cmpq %rsi,%rax
	je Append.append.badPtr
L.25:
	movq %rax,%r9
	movq %r14,%rsi
	addq %rdx,%rsi
	movq %rsi,%r8
	xorq %rsi,%rsi
	cmpq %rsi,%r8
	jl Append.append.badSub
L.26:
	movq 8(%r9),%rsi
	cmpq %rsi,%r8
	jge Append.append.badSub
L.27:
	movq %r12,%rdi
	xorq %rsi,%rsi
	cmpq %rsi,%rdi
	je Append.append.badPtr
L.28:
#	movq %rdi,%rdi
	movq %rdx,%rsi
	xorq %r10,%r10
	cmpq %r10,%rsi
	jl Append.append.badSub
L.29:
	movq 8(%rdi),%r10
	cmpq %r10,%rsi
	jge Append.append.badSub
L.30:
	movq 0(%r9),%r9
#	movq %r9,%r9
	addq %r8,%r9
	movq 0(%rdi),%rdi
#	movq %rdi,%rdi
	addq %rsi,%rdi
	movsbq 0(%rdi),%rsi
	movq %rsi,0(%r9)
#	movq %rdx,%rdx
	addq %rbx,%rdx
#	movq %rdx,%rdx
	jmp L.23
L.6:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	movq -24(%rbp),%r13
	movq -32(%rbp),%r14
	movq -40(%rbp),%r15
	addq $48,%rsp
	popq %rbp
	ret
Append.append.badPtr:
	call badPtr
Append.append.badSub:
	call badSub
	.data
	.balign 8
L.34:
	.byte 72
	.byte 101
	.byte 108
	.byte 108
	.byte 111
	.byte 0
	.data
	.balign 8
L.35:
	.quad L.34
	.quad 6
	.data
	.balign 8
L.36:
	.byte 32
	.byte 0
	.data
	.balign 8
L.37:
	.quad L.36
	.quad 2
	.data
	.balign 8
L.38:
	.byte 87
	.byte 111
	.byte 114
	.byte 108
	.byte 100
	.byte 33
	.byte 0
	.data
	.balign 8
L.39:
	.quad L.38
	.quad 7
	.text
Append:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.42:
	leaq L.35(%rip),%rax
#	movq %rax,%rax
	movq %rax,%rdi
	leaq L.37(%rip),%rax
	movq %rax,%rsi
	call Append.append
#	movq %rax,%rax
	movq %rax,%rdi
	leaq L.39(%rip),%rax
	movq %rax,%rsi
	call Append.append
#	movq %rax,%rax
	movq %rax,%rdi
	call Append.println
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
